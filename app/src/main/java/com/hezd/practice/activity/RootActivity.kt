package com.hezd.practice.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hezd.practice.TAG_LIFECYCLE
import com.hezd.practice.databinding.ActivityRootBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * 调试Activity启动流程
 * 进程之前流转：
 * |--App AActivity
 *   |--AMS
 *      |--App ActivityThread->performLaunchActivity
 *          |-- App BActivity
 *
 */

class RootActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRootBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)

        initClickListeners(binding)

//        testLifecycle(binding)
    }

    private fun initClickListeners(binding: ActivityRootBinding) {
        binding.buttonWebViewCrash.setOnClickListener {
            startActivity(Intent(this, WebViewRenderActivity::class.java))
        }

        binding.buttonAppStore.setOnClickListener {
            startActivity(Intent(this, AppStoreActivity::class.java))
        }
    }

//    private fun testLifecycle(binding: ActivityRootBinding) {
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                Log.d(TAG_LIFECYCLE, "state:started")
//                request().collect {
//                    binding.button.text = it
//                    Log.d(TAG_LIFECYCLE, "update ui")
//                }
//            }
//        }
//        log("lifecycle:onCreate")
//    }

    private fun log(msg:String) {
        Log.d(TAG_LIFECYCLE, msg)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        outState.putString("testkey","testvalue")
        log("onSaveInstanceState")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        log("onRestoreInstanceState")
//        val value = savedInstanceState["testkey"]
//        log("resotre value:$value")
    }

    private fun request(): Flow<String> {
       return flow {
            emit("result")
        }.onEach {
//            delay(5000)
        }
    }

    override fun onStart() {
        super.onStart()
        log("lifecycle:onStart")
    }

    override fun onResume() {
        super.onResume()
        log("lifecycle:onResume")
    }

    override fun onPause() {
        super.onPause()
        log("lifecycle:onPause")
    }

    override fun onStop() {
        super.onStop()
        log("lifecycle:onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG_LIFECYCLE,"${javaClass.simpleName}:onDestroy")
    }
}