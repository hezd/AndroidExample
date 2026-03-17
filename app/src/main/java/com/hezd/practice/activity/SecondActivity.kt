package com.hezd.practice.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hezd.practice.databinding.ActivitySecondBinding

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySecondBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)


    }
}