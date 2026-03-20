package com.hezd.practice.activity.test

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.hezd.practice.databinding.ActivityMainTestBinding

/**
 * 测试 Activity 任务栈（Task Stack）与启动模式的验证入口。
 *
 * 背景：
 * 用于验证 Android 中 singleTask 模式配合 taskAffinity 属性产生的特殊栈行为。
 * 原描述认为该跳转流程（Main->Second->Third->Main->Second）结束时，按下 Back 键可直接回到桌面，
 * 本测试旨在通过 Logcat 打印 taskId 及生命周期，揭示真实的返回栈出栈逻辑。
 *
 * 预期行为：
 * 按下 Back 键时，Activity 会根据任务栈顺序逐一出栈，通常需要多次 Back 才能回到桌面，
 * 而非一次性直接回到桌面。
 *
 * 验证步骤：
 * 1. 依次点击按钮跳转：MainTest -> SecondTest -> ThirdTest -> MainTest -> SecondTest。
 * 2. 在 Logcat 中过滤 "TaskTest" 标签，观察不同 Activity 的 taskId 及 onCreate/onResume 回调。
 * 3. 观察按下 Back 键后，页面返回的 Activity 顺序。
 */
class MainTestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.textView.text = "MainTestActivity\ntaskId: $taskId"
        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, SecondTestActivity::class.java))
        }
        
        Log.d("TaskTest", "MainTestActivity onCreate, taskId: $taskId")
    }

    override fun onResume() {
        super.onResume()
        Log.d("TaskTest", "MainTestActivity onResume, taskId: $taskId")
    }
}