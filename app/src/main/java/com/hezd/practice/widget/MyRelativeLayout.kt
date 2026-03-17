package com.hezd.practice.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.RelativeLayout
import com.hezd.practice.activity.EventDispatchActivity
import timber.log.Timber

/**
 * @author hezd
 * @date 2023/11/21 13:17
 * @description
 */
class MyRelativeLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr, defStyleRes) {
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        Timber.tag(EventDispatchActivity.TAG)
            .d("${this::class.java.simpleName} \n onToucheEvent,event=${MotionEvent.actionToString(event.action)}")
        return super.onTouchEvent(event)
    }
}