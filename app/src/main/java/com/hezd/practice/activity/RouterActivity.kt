package com.hezd.practice.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hezd.practice.activity.TestTouchActivity
import com.hezd.practice.databinding.ActivityRouterBinding

/**
 * @author hezd
 * @date 2026/3/25
 * @description 路由页面，负责跳转到各个功能测试模块
 */
class RouterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRouterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置触摸事件测试的导航按钮
        binding.btnToTouchTest.setOnClickListener {
            val intent = Intent(this, TestTouchActivity::class.java)
            startActivity(intent)
        }
    }
}
