package com.hezd.practice.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hezd.practice.databinding.ActivityRepeatLifecycleBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * @author hezd
 * @date 2023/8/3 10:55
 * @description
 */
class RepeatLifeCycleActivity :AppCompatActivity(){
    private var repeatJob: Job?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRepeatLifecycleBinding.inflate(layoutInflater)
        val contentView = binding.root
        setContentView(contentView)
        for(index in 1..3){
            repeatExecution()
        }
    }

    override fun onResume() {
        super.onResume()
        println("repeatOnLifecycle:onResumed")
    }

    private fun repeatExecution(){
        repeatJob?.cancel()
        repeatJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED){
                println("repeatOnLifecycle:execution")
            }
        }
    }
}