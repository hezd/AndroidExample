package com.hezd.practice

import android.app.Application
import com.hezd.dnsresolver.CustomDnsNetworkRegister
import timber.log.Timber

/**
 * @author hezd
 * @date 2023/9/26 09:51
 * @description
 */
class ExampleApplication:Application() {


//    private var connectivityManager: ConnectivityManager? = null
//    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())


//        // 1. 初始化 AndroidResolverConfigProvider
//        // 这是 dnsjava 在 Android 上获取系统 DNS 配置的关键。
//        // 它允许 dnsjava 访问系统级别的网络服务，从而发现当前配置的 DNS 服务器。
//        AndroidResolverConfigProvider.setContext(this)
//
//
//        // 2. 注册网络变化回调，以便在网络变化时刷新 DNS 配置
//        // 当网络类型切换（例如Wi-Fi到移动数据）或IP地址/DNS服务器信息更新时，
//        // 刷新 ResolverConfig 能够确保 dnsjava 使用最新的系统DNS服务器列表。
//        registerNetworkChangeCallback()
        CustomDnsNetworkRegister.register(this)
    }

//    private fun registerNetworkChangeCallback() {
//        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        networkCallback = object : ConnectivityManager.NetworkCallback() {
//            override fun onAvailable(network: Network) {
//                super.onAvailable(network)
//                // 网络可用时，刷新 dnsjava 的解析器配置
//                ResolverConfig.refresh()
//                println("Network available. dnsjava ResolverConfig refreshed.")
//                // 此时也可以选择性地清除 CustomDns 中的缓存，以防旧IP地址失效
//                CustomDns.clearCache()
//                println("CustomDns cache cleared due to network change.")
//            }
//
//            override fun onLost(network: Network) {
//                super.onLost(network)
//                // 网络丢失时，也刷新 dnsjava 的解析器配置
//                ResolverConfig.refresh()
//                println("Network lost. dnsjava ResolverConfig refreshed.")
//                // 此时清除 CustomDns 中的缓存可能更有必要
//                CustomDns.clearCache()
//                println("CustomDns cache cleared due to network change.")
//            }
//            // 可以根据需要重写更多方法，例如 onCapabilitiesChanged, onLinkPropertiesChanged 等
//            // 在这些方法中也调用 ResolverConfig.refresh() 是一个好习惯，以应对更细粒度的网络变化
//            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
//                super.onCapabilitiesChanged(network, networkCapabilities)
//                ResolverConfig.refresh()
//                println("Network capabilities changed. dnsjava ResolverConfig refreshed.")
//            }
//        }
//
//        val networkRequest = NetworkRequest.Builder()
//            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//            .build()
//
//        connectivityManager?.registerNetworkCallback(networkRequest, networkCallback!!)
//    }

    override fun onTerminate() {
        super.onTerminate()
        CustomDnsNetworkRegister.unRegister()
        // 在应用终止时解注册回调，防止内存泄漏
//        unregisterNetworkChangeCallback()
//        println("YourApplication onTerminate: NetworkCallback unregistered.")
    }

//    private fun unregisterNetworkChangeCallback() {
//        networkCallback?.let {
//            connectivityManager?.unregisterNetworkCallback(it)
//        }
//        networkCallback = null
//        connectivityManager = null
//    }
}