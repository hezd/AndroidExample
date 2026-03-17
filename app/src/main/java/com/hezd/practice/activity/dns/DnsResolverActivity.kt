package com.hezd.practice.activity.dns

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.hezd.dnsresolver.CustomDns
import com.hezd.practice.databinding.ActivityDnsResolverBinding
import com.hezd.practice.dns.OkHttpClientProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.net.UnknownHostException

/**
 * @author hezd
 * @date 2025/9/1
 * @description
 */
class DnsResolverActivity: AppCompatActivity() {
    private val binding:ActivityDnsResolverBinding by lazy {
        ActivityDnsResolverBinding.inflate(LayoutInflater.from(this))
    }

    private val scope = CoroutineScope(Dispatchers.Main + Job()) // UI 协程作用域
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.lookupButton.setOnClickListener {
            // 示例域名
            val hostname = "www.baidu.com" // 尝试一个既有IPv4也有IPv6的域名
            lookupHost(hostname)
        }

        binding.lookupApiButton.setOnClickListener {
            // 示例域名
            val hostname = "api.teyuntong.net" // 尝试一个既有IPv4也有IPv6的域名
            lookupHost(hostname)
        }


        binding.lookupACkButton.setOnClickListener {
            // 示例域名
            val hostname = "www.teyuntong.net" // 尝试一个既有IPv4也有IPv6的域名
            lookupHost(hostname)
        }

        binding.lookupAlBButton.setOnClickListener {
            // 示例域名
            val hostname = "alb-4tw21kpm1nmi9u426k.cn-beijing.alb.aliyuncs.com" // 尝试一个既有IPv4也有IPv6的域名
            lookupHost(hostname)
        }

        binding.clearCacheButton.setOnClickListener {
            CustomDns.clearCache() // 清空所有缓存
            binding.resultTextView.text = "DNS Cache Cleared!"
        }
    }

    private fun lookupHost(hostname: String) {
        binding.resultTextView.text = "Looking up $hostname..."
        scope.launch(Dispatchers.IO) { // 在 IO 协程中执行网络操作
            try {
                val client = OkHttpClientProvider.okHttpClient
                val request = Request.Builder()
                    .url("https://$hostname") // 使用HTTPS
                    .build()

                // 执行一个实际的网络请求，触发 CustomDns 的 lookup 方法
                client.newCall(request).execute().use { response ->
                    val ipAddresses = client.dns.lookup(hostname) // 获取实际解析到的IP
                    withContext(Dispatchers.Main) { // 回到主线程更新 UI
                        if (response.isSuccessful) {
                            binding.resultTextView.text = "Successfully connected to $hostname.\nResolved IPs: ${ipAddresses.joinToString { it.hostAddress }}\nHTTP Status: ${response.code}"
                            println("HTTP Request successful to $hostname. Response Code: ${response.code}")
                        } else {
                            binding.resultTextView.text = "HTTP Request failed for $hostname. Status: ${response.code} ${response.message}"
                            println("HTTP Request failed to $hostname. Status: ${response.code} ${response.message}")
                        }
                    }
                }
            } catch (e: UnknownHostException) {
                withContext(Dispatchers.Main) {
                    binding.resultTextView.text = "DNS Lookup Failed for $hostname: ${e.message}"
                    println("DNS Lookup Failed for $hostname: ${e.message}")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.resultTextView.text = "Network Error for $hostname: ${e.message}"
                    println("Network Error for $hostname: ${e.message}")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel() // 取消所有在作用域内的协程，防止内存泄漏
    }
}