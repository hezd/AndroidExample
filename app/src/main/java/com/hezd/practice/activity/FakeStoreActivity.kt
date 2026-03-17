package com.hezd.practice.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hezd.practice.databinding.ActivityFakeStoreBinding

/**
 * @author hezd
 * @date 2026/3/11
 * @description
 */
class FakeStoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityFakeStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}