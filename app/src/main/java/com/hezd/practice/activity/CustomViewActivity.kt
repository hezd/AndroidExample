package com.hezd.practice.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hezd.practice.R
import com.hezd.practice.databinding.ActivityCustomViewBinding

class CustomViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCustomViewBinding.inflate(layoutInflater)
        val contentView = binding.root
        setContentView(contentView)

    }
}