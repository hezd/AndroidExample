package com.hezd.practice.dns

import android.annotation.SuppressLint
import android.util.Log
import com.hezd.dnsresolver.CustomDns
import com.hezd.dnsresolver.DnsExecutionStrategy
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * @author hezd
 * @date 2025/9/1
 * @description
 */
@SuppressLint("LogNotTimber")
object OkHttpClientProvider {
    val okHttpClient: OkHttpClient by lazy {
        val customDns = CustomDns.Builder().apply {
            enableLogging(false)
            dnsQueryTimeout(2)
            dnsExecutionStrategy(DnsExecutionStrategy.CUSTOM_THEN_SYSTEM)
            onSystemDnsFailed { hostname, e, durationMillis ->
                Log.w(
                    CustomDns.TAG,
                    "System DNS lookup failed for $hostname in ${durationMillis}ms: ${e.message}."
                )
            }
            onCustomDnsFailed { hostname, currentAttemptedServers, e, durationMillis ->
                Log.w(
                    CustomDns.TAG,
                    "All custom DNS resolution attempts failed for $hostname. Custom DNS attempts took $durationMillis ms."
                )
            }
            onCustomDnsSuccess { hostname, dnsServerIp, addresses, durationMillis ->
                Log.d(
                    CustomDns.TAG,
                    "Custom DNS ($dnsServerIp) successfully resolved $hostname with: $addresses in ${durationMillis}ms."
                )
            }
        }.build()
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS) // 连接超时
            .readTimeout(10, TimeUnit.SECONDS)    // 读取超时
            .writeTimeout(10, TimeUnit.SECONDS)   // 写入超时
            .dns(customDns) // 将你的 CustomDns 实例设置给 OkHttpClient
            .build()
    }
}