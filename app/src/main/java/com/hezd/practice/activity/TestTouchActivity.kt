package com.hezd.practice.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hezd.practice.databinding.ActivityTestTouchBinding

/**
 * @author hezd
 * @date 2026/3/25
 * @description
 * 历史背景：
 * 本 Activity 用于测试 Android 事件分发机制。
 * 场景：ViewGroup 中有一个 View，两者均设置了点击事件。
 * 目的：通过日志模拟并验证以下情况：
 * 1. 当手指在 View 上按下并滑动时，若触发了 ViewGroup 的拦截 (onInterceptTouchEvent 返回 true)，
 *    View 会收到 ACTION_CANCEL 事件，导致原始点击事件被取消。
 * 2. 观察 ViewGroup 与 View 在整个滑动过程中的事件流传递 (dispatchTouchEvent & onTouchEvent)。
 */
class TestTouchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestTouchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestTouchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 使用 ViewBinding 获取组件
        val container = binding.container
        val childView = binding.childView

        // 设置点击事件以验证点击效果
        container.setOnClickListener {
            Log.d("TouchTest_Event", "ViewGroup 被点击了")
            Toast.makeText(this, "ViewGroup Clicked", Toast.LENGTH_SHORT).show()
        }

        childView.setOnClickListener {
            Log.d("TouchTest_Event", "ChildView 被点击了")
            Toast.makeText(this, "ChildView Clicked", Toast.LENGTH_SHORT).show()
        }
    }
}