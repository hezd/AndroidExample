package com.hezd.dnsresolver

import android.annotation.SuppressLint
import android.util.Log
import okhttp3.Dns
import org.xbill.DNS.AAAARecord
import org.xbill.DNS.ARecord
import org.xbill.DNS.Lookup
import org.xbill.DNS.SimpleResolver
import org.xbill.DNS.Type
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 定义DNS解析的执行策略。
 */
enum class DnsExecutionStrategy {
    /** 优先尝试系统DNS，失败后回退到自定义DNS（默认行为） */
    SYSTEM_THEN_CUSTOM,

    /** 优先尝试自定义DNS，失败后回退到系统DNS */
    CUSTOM_THEN_SYSTEM,

    /** 仅使用系统DNS */
    ONLY_SYSTEM,

    /** 仅使用自定义DNS */
    ONLY_CUSTOM
}

/**
 * @author hezd
 * @date 2025/9/1
 * @description 自定义dns解析
 */
@Suppress("DEPRECATION")
@SuppressLint("LogNotTimber")
class CustomDns private constructor(builder: Builder) : Dns {
    private val systemDns = Dns.SYSTEM

    private val customDnsServers: List<String> = builder.customDnsServers.toList()
    private val dnsQueryTimeoutSeconds: Int = builder.dnsQueryTimeoutSeconds

    /** 成功解析结果的缓存存活时间（毫秒）。在此期间内，同一域名的解析将直接返回缓存值。 */
    private val cacheExpirationMillis: Long =
        builder.cacheExpirationMinutes.toLong() * TimeUnit.MINUTES.toMillis(1)

    /** 失败解析结果的缓存存活时间（毫秒）。用于短时间内拦截重试，防止因解析失败导致的频繁无效网络请求。 */
    private val failedCacheExpirationMillis: Long =
        builder.failedCacheExpirationSeconds.toLong() * TimeUnit.SECONDS.toMillis(1)

    private val enableIpv6: Boolean = builder.enableIpv6

    /** 日志开关，开启后将输出详细的 DNS 解析过程和耗时信息。 */
    private val enableLogging: Boolean = builder.enableLogging

    /** 定义 DNS 解析的优先级策略（如：优先系统、优先自定义等）。 */
    private val dnsExecutionStrategy: DnsExecutionStrategy = builder.dnsExecutionStrategy // 新增执行策略

    // 回调函数 - 修改 onSystemDnsFailed 的类型，增加 durationMillis 参数
    private val onSystemDnsFailed: ((hostname: String, e: Exception, durationMillis: Long) -> Unit)? =
        builder.onSystemDnsFailed

    // 修改 onCustomDnsSuccess 的类型，增加 durationMillis 参数
    private val onCustomDnsSuccess: ((hostname: String, dnsServerIp: String, addresses: List<InetAddress>, durationMillis: Long) -> Unit)? =
        builder.onCustomDnsSuccess
    private val onCustomDnsFailed: ((hostname: String, currentAttemptedServers: List<String>, e: Exception?, durationMillis: Long) -> Unit)? =
        builder.onCustomDnsFailed
    private val onAllDnsFailed: ((hostname: String, e: UnknownHostException) -> Unit)? =
        builder.onAllDnsFailed

    // 封装日志输出方法
    private fun logD(message: String) {
        if (enableLogging) {
            Log.d(TAG, message)
        }
    }

    private fun logW(message: String) {
        if (enableLogging) {
            Log.w(TAG, message)
        }
    }

    private fun logE(message: String, throwable: Throwable? = null) {
        if (enableLogging) {
            if (throwable != null) {
                Log.e(TAG, message, throwable)
            } else {
                Log.e(TAG, message)
            }
        }
    }


    companion object {
        private val dnsCache = ConcurrentHashMap<String, CacheEntry>()

        // 定义日志 Tag
        const val TAG = "CustomDns"

        // CacheEntry 的 creationTime 依然使用 System.currentTimeMillis()，因为缓存过期是基于挂钟时间的
        private data class CacheEntry(
            val addresses: List<InetAddress>,
            val creationTime: Long,
            val isFailure: Boolean = false,
            val expirationDuration: Long
        )

        /**
         * 供外部（例如 Application 类）调用，用于清空整个 DNS 缓存。
         * 在网络状态变化时调用此方法非常有用，以避免使用过期的IP地址。
         * 注意：静态方法无法直接访问 enableLogging，因此总是会打印清空缓存的日志。
         */
        @JvmStatic
        fun clearCache() {
            dnsCache.clear()
            Log.d(TAG, "All cached DNS entries cleared.")
        }
    }

    override fun lookup(hostname: String): List<InetAddress> {
        val overallStartTimeNanos = System.nanoTime() // 使用 nanoTime 测量总耗时
        logD("Starting DNS lookup for $hostname with strategy: $dnsExecutionStrategy...")

        // 1. 尝试从缓存获取
        val cachedEntry = getFromCache(hostname)
        if (cachedEntry != null) {
            if (!cachedEntry.isFailure) { // 如果是成功的缓存且未过期
                val cacheHitTimeNanos = System.nanoTime()
                val cacheLookupDurationMillis =
                    (cacheHitTimeNanos - overallStartTimeNanos) / 1_000_000L
                logD("Cache hit (success) for $hostname -> ${cachedEntry.addresses}. Cache lookup took ${cacheLookupDurationMillis}ms. Total lookup took ${cacheLookupDurationMillis}ms.")
                return cachedEntry.addresses
            } else { // 如果是失败的缓存且未过期
                logW("Cache hit (failed entry) for $hostname (not expired). Retrying DNS resolution process.")
                // 命中失败缓存，不直接返回，而是继续尝试实际的DNS解析，以防网络恢复
            }
        }

        // 根据执行策略来决定 DNS 解析顺序
        val resolvedAddresses: List<InetAddress>? = when (dnsExecutionStrategy) {
            DnsExecutionStrategy.SYSTEM_THEN_CUSTOM -> {
                performSystemDnsLookup(hostname) ?: performCustomDnsLookup(hostname)
            }

            DnsExecutionStrategy.CUSTOM_THEN_SYSTEM -> {
                performCustomDnsLookup(hostname) ?: performSystemDnsLookup(hostname)
            }

            DnsExecutionStrategy.ONLY_SYSTEM -> {
                performSystemDnsLookup(hostname)
            }

            DnsExecutionStrategy.ONLY_CUSTOM -> {
                performCustomDnsLookup(hostname)
            }
        }

        if (resolvedAddresses != null) {
            logD("Total lookup for $hostname took ${(System.nanoTime() - overallStartTimeNanos) / 1_000_000L}ms.")
            return resolvedAddresses
        } else {
            // 所有解析方法都失败，则缓存失败信息并抛出异常
            val finalException = UnknownHostException(
                "Failed to resolve $hostname using strategy $dnsExecutionStrategy. " +
                        "Last system DNS error: ${latestSystemDnsException?.message ?: "None"}. " +
                        "Last custom DNS error: ${latestCustomDnsException?.message ?: "None"}"
            )
            putIntoCache(
                hostname,
                emptyList(),
                isFailure = true,
                expiration = failedCacheExpirationMillis
            )

            // 如果是 ONLY_SYSTEM 且失败，则没有 customDnsLastError
            // 如果是 ONLY_CUSTOM 且失败，则没有 systemDnsException
            onAllDnsFailed?.invoke(hostname, finalException)
            logD("Total lookup for $hostname took ${(System.nanoTime() - overallStartTimeNanos) / 1_000_000L}ms (failed).")

            throw finalException
        }
    }

    // 用于在所有策略都失败时，收集最近的异常信息
    private var latestSystemDnsException: Exception? = null
    private var latestCustomDnsException: Exception? = null


    /**
     * 执行系统 DNS 解析。
     * @return 解析到的地址列表，如果失败或为空则返回 null。
     */
    private fun performSystemDnsLookup(hostname: String): List<InetAddress>? {
        val systemDnsStartTimeNanos = System.nanoTime()
        latestSystemDnsException = null // 重置
        try {
            val systemAddresses = systemDns.lookup(hostname)
            val systemDnsEndTimeNanos = System.nanoTime()
            val systemDnsDurationMillis =
                (systemDnsEndTimeNanos - systemDnsStartTimeNanos) / 1_000_000L

            if (systemAddresses.isNotEmpty()) {
                logD("System DNS successfully resolved $hostname with: $systemAddresses in ${systemDnsDurationMillis}ms.")
                putIntoCache(
                    hostname,
                    systemAddresses,
                    isFailure = false,
                    expiration = cacheExpirationMillis
                )
                return systemAddresses
            } else {
                logW("System DNS returned empty list for $hostname in ${systemDnsDurationMillis}ms. No valid addresses found.")
                latestSystemDnsException =
                    UnknownHostException("System DNS returned empty list for $hostname")
                onSystemDnsFailed?.invoke(
                    hostname,
                    latestSystemDnsException!!,
                    systemDnsDurationMillis
                )
                return null
            }
        } catch (e: UnknownHostException) {
            val systemDnsEndTimeNanos = System.nanoTime()
            val systemDnsDurationMillis =
                (systemDnsEndTimeNanos - systemDnsStartTimeNanos) / 1_000_000L
            latestSystemDnsException = e
            logW("System DNS lookup failed for $hostname in ${systemDnsDurationMillis}ms: ${e.message}.")
            onSystemDnsFailed?.invoke(hostname, e, systemDnsDurationMillis)
            return null
        } catch (e: Exception) {
            val systemDnsEndTimeNanos = System.nanoTime()
            val systemDnsDurationMillis =
                (systemDnsEndTimeNanos - systemDnsStartTimeNanos) / 1_000_000L
            latestSystemDnsException = e
            logE(
                "Unexpected error from System DNS for $hostname in ${systemDnsDurationMillis}ms: ${e.message}",
                e
            )
            onSystemDnsFailed?.invoke(hostname, e, systemDnsDurationMillis)
            return null
        }
    }

    /**
     * 执行自定义 DNS 解析。
     * @return 解析到的地址列表，如果所有自定义服务器都失败或为空则返回 null。
     */
    private fun performCustomDnsLookup(hostname: String): List<InetAddress>? {
        val customResolvedAddresses = mutableListOf<InetAddress>()
        val attemptedCustomServers = mutableListOf<String>()
        latestCustomDnsException = null // 重置

        val customDnsOverallStartTimeNanos = System.nanoTime()

        for (dnsServerIp in customDnsServers) {
            attemptedCustomServers.add(dnsServerIp)
            val currentCustomDnsServerStartTimeNanos = System.nanoTime()
            try {
                val resolver = SimpleResolver(dnsServerIp)
                resolver.setTimeout(dnsQueryTimeoutSeconds)

                val lookupA = Lookup(hostname, Type.A)
                lookupA.setResolver(resolver)
                lookupA.run()

                var currentServerFoundAddress = false

                if (lookupA.result == Lookup.SUCCESSFUL) {
                    lookupA.answers?.forEach { record ->
                        if (record is ARecord) {
                            val address = InetAddress.getByName(record.address.hostAddress)
                            if (!customResolvedAddresses.contains(address)) {
                                customResolvedAddresses.add(address)
                            }
                            currentServerFoundAddress = true
                        }
                    }
                } else {
                    logW("Custom DNS ($dnsServerIp) A record lookup failed for $hostname: ${lookupA.errorString}")
                }

                if (enableIpv6) {
                    val lookupAAAA = Lookup(hostname, Type.AAAA)
                    lookupAAAA.setResolver(resolver)
                    lookupAAAA.run()

                    if (lookupAAAA.result == Lookup.SUCCESSFUL) {
                        lookupAAAA.answers?.forEach { record ->
                            if (record is AAAARecord) {
                                if (!customResolvedAddresses.contains(record.address)) {
                                    customResolvedAddresses.add(record.address)
                                }
                                currentServerFoundAddress = true
                            }
                        }
                    } else {
                        logW("Custom DNS ($dnsServerIp) AAAA record lookup failed for $hostname: ${lookupAAAA.errorString}")
                    }
                }

                val currentCustomDnsServerEndTimeNanos = System.nanoTime()
                val currentCustomDnsServerDurationMillis =
                    (currentCustomDnsServerEndTimeNanos - currentCustomDnsServerStartTimeNanos) / 1_000_000L
                if (currentServerFoundAddress && customResolvedAddresses.isNotEmpty()) {
                    val finalAddresses = customResolvedAddresses.distinct()
                    logD("Custom DNS ($dnsServerIp) successfully resolved $hostname with: $finalAddresses in ${currentCustomDnsServerDurationMillis}ms.")
                    putIntoCache(
                        hostname,
                        finalAddresses,
                        isFailure = false,
                        expiration = cacheExpirationMillis
                    )
                    onCustomDnsSuccess?.invoke(
                        hostname,
                        dnsServerIp,
                        finalAddresses,
                        currentCustomDnsServerDurationMillis
                    )
                    return Collections.unmodifiableList(finalAddresses)
                } else {
                    logW("Custom DNS ($dnsServerIp) failed to resolve $hostname in ${currentCustomDnsServerDurationMillis}ms.")
                    latestCustomDnsException =
                        UnknownHostException("Custom DNS ($dnsServerIp) failed to resolve $hostname or returned empty list.")
                }

            } catch (e: Exception) {
                val currentCustomDnsServerEndTimeNanos = System.nanoTime()
                val currentCustomDnsServerDurationMillis =
                    (currentCustomDnsServerEndTimeNanos - currentCustomDnsServerStartTimeNanos) / 1_000_000L
                latestCustomDnsException = e
                logE(
                    "Custom DNS ($dnsServerIp) error for $hostname in ${currentCustomDnsServerDurationMillis}ms: ${e.message}",
                    e
                )
            }
        }

        // 如果所有自定义 DNS 服务器都尝试了，但没有一个成功
        val totalCustomDnsTimeMillis =
            (System.nanoTime() - customDnsOverallStartTimeNanos) / 1_000_000L
        logW("All custom DNS resolution attempts failed for $hostname. Custom DNS attempts took $totalCustomDnsTimeMillis ms.")
        onCustomDnsFailed?.invoke(
            hostname,
            attemptedCustomServers,
            latestCustomDnsException,
            totalCustomDnsTimeMillis
        )
        return null
    }


    private fun getFromCache(hostname: String): CacheEntry? {
        val entry = dnsCache[hostname]
        if (entry != null) {
            // 缓存过期判断依然使用 currentTimeMillis，因为过期时间本身就是基于挂钟时间的
            if (System.currentTimeMillis() - entry.creationTime < entry.expirationDuration) {
                return entry // 未过期，返回缓存项（可能是成功或失败）
            } else {
                // 已过期，移除缓存
                logW("Cache expired for $hostname (isFailure: ${entry.isFailure}), removing.")
                dnsCache.remove(hostname)
            }
        }
        return null // 缓存不存在或已过期
    }

    private fun putIntoCache(
        hostname: String,
        addresses: List<InetAddress>,
        isFailure: Boolean,
        expiration: Long
    ) {
        val entry = CacheEntry(
            addresses,
            System.currentTimeMillis(),
            isFailure,
            expiration
        ) // creationTime 仍使用 currentTimeMillis
        dnsCache[hostname] = entry
        logD("Cached $hostname -> $addresses (isFailure: $isFailure) with expiration ${expiration / (1000 * 60)} minutes.")
    }

    fun invalidateCache(hostname: String) {
        dnsCache.remove(hostname)
        logD("Cache invalidated for $hostname.")
    }

    /**
     * CustomDns 的 Builder 类。
     * 用于灵活配置 CustomDns 实例的各项参数。
     */
    class Builder {
        internal var dnsQueryTimeoutSeconds: Int = 3

        /** 成功解析后的缓存时间（单位：分钟），默认 5 分钟。 */
        internal var cacheExpirationMinutes: Int = 5

        /** 解析失败后的缓存时间（单位：秒），默认 30 秒，防止频繁重试。 */
        internal var failedCacheExpirationSeconds: Int = 30

        internal var enableIpv6: Boolean = false
        internal var enableLogging: Boolean = true
        internal var dnsExecutionStrategy: DnsExecutionStrategy =
            DnsExecutionStrategy.SYSTEM_THEN_CUSTOM // 新增策略配置，默认优先系统DNS

        internal val customDnsServers: MutableList<String> = mutableListOf(
            "119.29.29.29", // 腾讯云公共DNS
            "223.5.5.5",    // 阿里云公共DNS
            "223.6.6.6",    // 阿里云公共DNS
            "1.2.4.8",      // DNSPod公共DNS
//            "8.8.8.8",      // Google Public DNS
//            "8.8.4.4",      // Google Public DNS
//            "9.9.9.9",      // Quad9 DNS (注重隐私和安全)
//            "149.112.112.112" // Quad9 DNS
        )

        internal var onSystemDnsFailed: ((hostname: String, e: Exception, durationMillis: Long) -> Unit)? =
            null
        internal var onCustomDnsSuccess: ((hostname: String, dnsServerIp: String, addresses: List<InetAddress>, durationMillis: Long) -> Unit)? =
            null
        internal var onCustomDnsFailed: ((hostname: String, currentAttemptedServers: List<String>, e: Exception?, durationMillis: Long) -> Unit)? =
            null
        internal var onAllDnsFailed: ((hostname: String, e: UnknownHostException) -> Unit)? = null

        fun dnsQueryTimeout(seconds: Int) = apply {
            this.dnsQueryTimeoutSeconds = seconds
        }

        fun cacheExpiration(minutes: Int) = apply {
            this.cacheExpirationMinutes = minutes
        }

        fun failedCacheExpiration(seconds: Int) = apply {
            this.failedCacheExpirationSeconds = seconds
        }

        fun enableIpv6(enable: Boolean) = apply {
            this.enableIpv6 = enable
        }

        fun enableLogging(enable: Boolean) = apply {
            this.enableLogging = enable
        }

        fun dnsExecutionStrategy(strategy: DnsExecutionStrategy) = apply {
            this.dnsExecutionStrategy = strategy
        }

        fun addCustomDnsServer(ip: String) = apply {
            if (!this.customDnsServers.contains(ip)) {
                this.customDnsServers.add(ip)
            }
        }

        fun setCustomDnsServers(servers: List<String>) = apply {
            this.customDnsServers.clear()
            this.customDnsServers.addAll(servers.distinct())
        }

        fun onSystemDnsFailed(callback: (hostname: String, e: Exception, durationMillis: Long) -> Unit) =
            apply {
                this.onSystemDnsFailed = callback
            }

        fun onCustomDnsSuccess(callback: (hostname: String, dnsServerIp: String, addresses: List<InetAddress>, durationMillis: Long) -> Unit) =
            apply {
                this.onCustomDnsSuccess = callback
            }

        fun onCustomDnsFailed(callback: (hostname: String, currentAttemptedServers: List<String>, e: Exception?, durationMillis: Long) -> Unit) =
            apply {
                this.onCustomDnsFailed = callback
            }

        fun onAllDnsFailed(callback: (hostname: String, e: UnknownHostException) -> Unit) =
            apply {
                this.onAllDnsFailed = callback
            }

        fun build(): CustomDns {
            return CustomDns(this)
        }
    }
}
