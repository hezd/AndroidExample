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
    private val enableIpv6: Boolean = builder.enableIpv6
    private val cacheExpirationMillis: Long =
        builder.cacheExpirationMinutes.toLong() * TimeUnit.MINUTES.toMillis(1)
    private val failedCacheExpirationMillis: Long =
        builder.failedCacheExpirationSeconds.toLong() * TimeUnit.SECONDS.toMillis(1)
    private val enableLogging: Boolean = builder.enableLogging // 日志开关
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
        internal var cacheExpirationMinutes: Int = 5
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

//
///**
// * @author hezd
// * @date 2025/9/1
// * @description
// */
//class CustomDns : Dns {
//    // OkHttp 的默认系统 DNS 解析器。
//    // 在 Android 环境中，Dns.SYSTEM 也是基于系统 DNS 服务进行解析的。
//    // 作为最终降级手段，它将依赖 Android 系统的DNS配置。
//    private val backupDns = Dns.SYSTEM
//
//    // 自定义公共 DNS 服务器 IP 地址列表
//    // 优先选择支持 EDNS Client Subnet (ECS) 的公共 DNS，以便获得更准确的 CDN 路由
//    private val customDnsServers = listOf(
//        "223.5.5.5",   // 阿里云公共DNS
//        "223.6.6.6",   // 阿里云公共DNS
//        "119.29.29.29",// 腾讯云公共DNS
//        "1.2.4.8",     // DNSPod公共DNS
////        "8.8.8.8",     // Google Public DNS (可能因为GFW访问不稳定)
////        "8.8.4.4"      // Google Public DNS
//    )
//
//    // DNS 查询超时时间 (毫秒)。SimpleResolver 超时单位是秒，所以需要转换。
//    private val DNS_QUERY_TIMEOUT_MILLIS = 3000L // 3秒超时
//    private val DNS_QUERY_TIMEOUT_SECONDS = 3 // 3秒超时
//
//    companion object {
//        // 静态缓存，允许在网络变化时由 Application 类清空
//        private val dnsCache = ConcurrentHashMap<String, CacheEntry>()
//        private val CACHE_EXPIRATION_MILLIS = TimeUnit.MINUTES.toMillis(5) // 缓存 5 分钟
//
//        // 定义缓存条目，包含地址列表和创建时间
//        private data class CacheEntry(val addresses: List<InetAddress>, val creationTime: Long)
//
//        /**
//         * 供外部（例如 Application 类）调用，用于清空整个 DNS 缓存。
//         * 在网络状态变化时调用此方法非常有用，以避免使用过期的IP地址。
//         */
//        @JvmStatic
//        fun clearCache() {
//            dnsCache.clear()
//            println("CustomDns: All cached DNS entries cleared.")
//        }
//    }
//
//    override fun lookup(hostname: String): List<InetAddress> {
//        // 1. 尝试从缓存获取
//        val cachedAddresses = getFromCache(hostname)
//        if (cachedAddresses != null) {
//            println("CustomDns: Cache hit for $hostname -> $cachedAddresses")
//            return cachedAddresses
//        }
//
//        val resolvedAddresses = mutableListOf<InetAddress>()
//        var customDnsFailedAllAttempts = true // 标记所有自定义 DNS 是否都失败了
//
//        // 2. 尝试使用自定义 DNS 服务器
//        // 遍历自定义 DNS 服务器列表，直到成功解析
//        for (dnsServerIp in customDnsServers) {
//            try {
//                val resolver = SimpleResolver(dnsServerIp)
//                resolver.setTimeout(DNS_QUERY_TIMEOUT_SECONDS)
////                (DNS_QUERY_TIMEOUT_MILLIS / 1000).toDuration(DurationUnit.SECONDS)
//
//                // 同时查询 A (IPv4) 和 AAAA (IPv6) 记录
//                val lookupA = Lookup(hostname, Type.A)
//                lookupA.setResolver(resolver)
//                lookupA.run()
//
//                val lookupAAAA = Lookup(hostname, Type.AAAA)
//                lookupAAAA.setResolver(resolver)
//                lookupAAAA.run()
//
//                var currentServerFoundAddress = false // 标记当前服务器是否成功解析到IP地址
//
//                if (lookupA.result == Lookup.SUCCESSFUL) {
//                    lookupA.answers?.forEach { record ->
//                        if (record is ARecord) {
//                            val address = record.address.hostAddress
//                            resolvedAddresses.add(InetAddress.getByName(address))
//                            currentServerFoundAddress = true
//                        }
//                    }
//                } else {
//                    println("Custom DNS ($dnsServerIp) A record lookup failed for $hostname: ${lookupA.errorString}")
//                }
//
////                if (lookupAAAA.result == Lookup.SUCCESSFUL) {
////                    lookupAAAA.answers?.forEach { record ->
////                        if (record is AAAARecord) {
////                            resolvedAddresses.add(record.address)
////                            currentServerFoundAddress = true
////                        }
////                    }
////                } else {
////                    println("Custom DNS ($dnsServerIp) AAAA record lookup failed for $hostname: ${lookupAAAA.errorString}")
////                }
//
//                // 如果当前自定义服务器成功解析到至少一个IP地址，则视为成功
//                if (currentServerFoundAddress && resolvedAddresses.isNotEmpty()) {
//                    customDnsFailedAllAttempts = false // 至少一个自定义 DNS 成功了
//                    println("Custom DNS ($dnsServerIp) successfully resolved $hostname with: $resolvedAddresses")
//                    break // 成功解析后，不再尝试其他自定义DNS
//                } else {
//                    // 如果当前服务器没解析到任何IP，清除之前可能因尝试其他服务器而添加的部分结果
//                    // 避免不完整的解析结果被缓存或返回
//                    resolvedAddresses.clear()
//                }
//
//            } catch (e: Exception) {
//                // 捕获连接超时、网络不可达等异常
//                println("Custom DNS ($dnsServerIp) error for $hostname: ${e.message}")
//                resolvedAddresses.clear() // 清除当前尝试的结果
//            }
//        }
//
//        // 3. 如果自定义 DNS 成功并有结果，则缓存并返回
//        if (!customDnsFailedAllAttempts && resolvedAddresses.isNotEmpty()) {
//            // 对结果进行去重并保持原始顺序 (或按 IPv4 -> IPv6 排序)
//            val uniqueAddresses = resolvedAddresses.distinct()
//            putIntoCache(hostname, uniqueAddresses)
//            return Collections.unmodifiableList(uniqueAddresses) // 返回不可变列表
//        }
//
//        // 4. 如果所有自定义 DNS 都失败或没有找到任何 IP 记录，回退到系统 DNS
//        println("CustomDns: All custom DNS failed/no IP records for $hostname, falling back to system DNS.")
//        return try {
//            val systemAddresses = backupDns.lookup(hostname)
//            // 缓存系统 DNS 的结果
//            if (systemAddresses.isNotEmpty()) {
//                putIntoCache(hostname, systemAddresses)
//            } else {
//                // 如果系统 DNS 也解析失败，将此失败信息也放入缓存，但设置一个短的过期时间
//                // 避免短时间内重复查询一个已知无法解析的域名
//                putIntoCache(hostname, emptyList(), shortExpiration = true)
//                println("CustomDns: System DNS also failed for $hostname. Cached empty list with short expiration.")
//            }
//            systemAddresses
//        } catch (e: UnknownHostException) {
//            // 系统 DNS 也解析失败
//            println("CustomDns: System DNS lookup failed for $hostname: ${e.message}")
//            // 将此失败信息放入缓存，但设置一个短的过期时间
//            putIntoCache(hostname, emptyList(), shortExpiration = true)
//            throw e // 重新抛出 OkHttp 期望的异常
//        } catch (e: Exception) {
//            // 其他系统 DNS 解析错误
//            println("CustomDns: System DNS lookup failed for $hostname due to an unexpected error: ${e.message}")
//            // 将此失败信息放入缓存，但设置一个短的过期时间
//            putIntoCache(hostname, emptyList(), shortExpiration = true)
//            throw UnknownHostException("System DNS lookup failed for $hostname: ${e.message}")
//        }
//    }
//
//    // 从缓存获取，并处理过期逻辑
//    private fun getFromCache(hostname: String): List<InetAddress>? {
//        val entry = dnsCache[hostname]
//        if (entry != null) {
//            if (System.currentTimeMillis() - entry.creationTime < CACHE_EXPIRATION_MILLIS) {
//                // 缓存有效
//                return entry.addresses
//            } else {
//                // 缓存过期，移除
//                println("CustomDns: Cache expired for $hostname, removing.")
//                dnsCache.remove(hostname)
//            }
//        }
//        return null
//    }
//
//    // 将解析结果放入缓存
//    private fun putIntoCache(hostname: String, addresses: List<InetAddress>, shortExpiration: Boolean = false) {
//        val expiration = if (shortExpiration) TimeUnit.SECONDS.toMillis(30) else CACHE_EXPIRATION_MILLIS // 短期缓存30秒
//        dnsCache[hostname] = CacheEntry(addresses, System.currentTimeMillis() + expiration) // 记录过期时间
//        println("CustomDns: Cached $hostname -> $addresses with expiration ${if (shortExpiration) "short" else "normal"}.")
//    }
//
//    // 在 CustomDns 中提供一个方法，用于清除单个域名的缓存
//    fun invalidateCache(hostname: String) {
//        dnsCache.remove(hostname)
//        println("CustomDns: Cache invalidated for $hostname.")
//    }
//}