package com.hezd.practice.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hezd.practice.databinding.ActivityDataFlowBinding
import com.hezd.practice.viewmodel.DataFlowViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * 测试Flow数据流
 */
class DataFlowActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityDataFlowBinding.inflate(layoutInflater)
    }

    private val viewModel : DataFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = binding.root
        setContentView(root)

        binding.textview.setOnClickListener {
            startActivity(Intent(DataFlowActivity@this,SecondActivity::class.java))
        }
        startTimer()
    }

    private fun startTimer() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED){
                viewModel.mutableStateFlow.collect {
                    println("collect data flow:$it")
                }
            }
        }
    }
}