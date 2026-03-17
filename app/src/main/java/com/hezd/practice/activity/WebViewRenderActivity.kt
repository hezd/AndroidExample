package com.hezd.practice.activity

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.hezd.practice.databinding.ActivityWebviewRenderBinding

/**
 * @author hezd
 * @date 2026/2/10
 * @description WebView渲染问题调试
 */
@SuppressLint("LogNotTimber")
class WebViewRenderActivity : AppCompatActivity() {
    companion object {
        const val TAG = "webView"

        /**
         * render进程崩溃重最大试次数
         */
        const val MAX_RETRIES_COUNT = 3
        const val TARGET_URL = "https://www.baidu.com"
        const val CRASH_URL = "chrome://crash"
    }

    /**
     * render进程崩溃重试次数
     */
    private var retryCount = 0

    lateinit var binding: ActivityWebviewRenderBinding
    private val loadUrlRunnable = Runnable {
        binding.webView.loadUrl(CRASH_URL)
    }
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        binding = ActivityWebviewRenderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initWebView()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        handler.removeCallbacks(loadUrlRunnable)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        Log.d(TAG, "initWebView")
        retryCount = 0
        val webView = binding.webView
        val settings: WebSettings = webView.getSettings()
        settings.allowUniversalAccessFromFileURLs = true
        settings.allowFileAccessFromFileURLs = true

        settings.setGeolocationEnabled(true)
        settings.allowFileAccess = true

        //让JavaScript自动打开窗口，默认false。适用于JavaScript方法window.open()。
        settings.javaScriptCanOpenWindowsAutomatically = true

        //设置WebView是否允许执行JavaScript脚本，默认false，不允许。
        settings.javaScriptEnabled = true

        // 使用localStorage则必须打开, 支持文件存储
        settings.domStorageEnabled = true

        //设置是否开启数据库存储API，默认false。
        settings.databaseEnabled = true

        //设置触摸可缩放  ，默认值为false。
        settings.builtInZoomControls = true

        //是否允许WebView度超出以概览的方式载入页面，默认false。即缩小内容以适应屏幕宽度
        settings.loadWithOverviewMode = true

        // 设置此属性，可任意比例缩放。
        settings.useWideViewPort = true

        //数据库存储API是否可用，默认值false。
        settings.databaseEnabled = true

        //WebView是否下载图片资源，默认为true。【此处需要为false。否则图片不展现】
        settings.blockNetworkImage = false

        //
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.textZoom = 100


        // android 5.0以上默认不支持Mixed Content
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE


        //清除网页访问留下的缓存
        //由于内核缓存是全局的因此这个方法不仅仅针对webview而是针对整个应用程序.
        webView.clearCache(true)


        //清除当前webview访问的历史记录
        //只会webview访问历史记录里的所有记录除了当前访问记录
        webView.clearHistory()

        //这个api仅仅清除自动完成填充的表单数据，并不会清除WebView存储到本地的数据
        webView.clearFormData()
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                Log.e(
                    TAG,
                    "onReceivedError: code=${error?.errorCode}, message=${error?.description}"
                )
            }

            override fun onRenderProcessGone(
                view: WebView?,
                detail: RenderProcessGoneDetail?
            ): Boolean {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || detail == null) {
                    return false
                }
                if (binding.webView == view) {
                    Log.e(TAG, "onRenderProcessGone: ${detail.didCrash()}")
                    val webViewPackageInfo = WebView.getCurrentWebViewPackage()
                    Log.e(
                        TAG,
                        "WebView Package Name: ${webViewPackageInfo?.packageName}，Version Name：${webViewPackageInfo?.versionName},Version Code: ${webViewPackageInfo?.versionCode}"
                    )

                    if (retryCount < MAX_RETRIES_COUNT) {
                        retryCount++
                        if (detail.didCrash()) {
                            view.clearCache(true)
                            handler.postDelayed(loadUrlRunnable, 2000) // 延迟 2 秒后重试
                        } else {
                            view.reload()
                        }
                    } else {
                        Log.e(TAG, "Max retries reached. Stopping reload attempts.")
                        // 显示错误提示或跳转到备用页面
                    }
                    return true
                }
                return super.onRenderProcessGone(view, detail)
            }
        }

        binding.webView.loadUrl(TARGET_URL)

        binding.buttonCrash.setOnClickListener {
            binding.webView.loadUrl(CRASH_URL)
        }
    }
}