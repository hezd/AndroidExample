package com.hezd.practice.activity.test

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout

class MyTestViewGroup(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val tag = "TouchTest_ViewGroup"

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        Log.d(tag, "dispatchTouchEvent: " + MotionEvent.actionToString(event.action))
        return super.dispatchTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        Log.d(tag, "onInterceptTouchEvent: "+MotionEvent.actionToString(event.action))
        return super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d(tag, "onTouchEvent: " + MotionEvent.actionToString(event.action))
        return super.onTouchEvent(event)
    }
}
