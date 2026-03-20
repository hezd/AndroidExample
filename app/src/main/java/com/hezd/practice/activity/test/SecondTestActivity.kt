package com.hezd.practice.activity.test

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.hezd.practice.databinding.ActivitySecondTestBinding

class SecondTestActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecondTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.textView.text = "SecondTestActivity\ntaskId: $taskId"
        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, ThirdTestActivity::class.java))
        }
        
        Log.d("TaskTest", "SecondTestActivity onCreate, taskId: $taskId")
    }

    override fun onResume() {
        super.onResume()
        Log.d("TaskTest", "SecondTestActivity onResume, taskId: $taskId")
    }
}