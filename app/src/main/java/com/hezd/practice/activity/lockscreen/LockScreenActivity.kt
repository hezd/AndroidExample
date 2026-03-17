package com.hezd.practice.activity.lockscreen

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.hezd.practice.databinding.ActivityLockScreenBinding

/**
 * @author hezd
 * @date 2024/6/28
 * @description
 */
class LockScreenActivity:AppCompatActivity() {
    lateinit var binding: ActivityLockScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        binding.button.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                // 请求锁屏显示权限
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                startActivityForResult(intent, 100)
            }
        }
    }
}