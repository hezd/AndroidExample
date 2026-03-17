package com.hezd.practice.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hezd.practice.databinding.ActivityGifLoaderBinding
import com.hezd.practice.databinding.ActivityProgressbarBinding

/**
 * @author hezd
 * @date 2025/1/23
 * @description
 */
class ProgressBarActivity : AppCompatActivity() {

    lateinit var binding:ActivityProgressbarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressbarBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}