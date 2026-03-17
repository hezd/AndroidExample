package com.hezd.practice.activity.edittext

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hezd.practice.databinding.ActivityEdittextBinding

/**
 * @author hezd
 * @date 2024/11/12
 * @description
 */
class EditTextActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEdittextBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEdittextBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}