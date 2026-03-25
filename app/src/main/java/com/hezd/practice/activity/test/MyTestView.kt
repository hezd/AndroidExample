package com.hezd.practice.activity.test

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatTextView

class MyTestView(context: Context, attrs: AttributeSet) : AppCompatTextView(context, attrs) {
    private val tag = "TouchTest_View"

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        Log.d(tag, "dispatchTouchEvent: " + MotionEvent.actionToString(event.action))
        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d(tag, "onTouchEvent: " + MotionEvent.actionToString(event.action))
        return super.onTouchEvent(event)
    }

}
