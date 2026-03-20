package com.hezd.practice.activity.test

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.hezd.practice.databinding.ActivityThirdTestBinding

class ThirdTestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityThirdTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThirdTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.textView.text = "ThirdTestActivity\ntaskId: $taskId"
        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, MainTestActivity::class.java))
        }
        
        Log.d("TaskTest", "ThirdTestActivity onCreate, taskId: $taskId")
    }

    override fun onResume() {
        super.onResume()
        Log.d("TaskTest", "ThirdTestActivity onResume, taskId: $taskId")
    }
}