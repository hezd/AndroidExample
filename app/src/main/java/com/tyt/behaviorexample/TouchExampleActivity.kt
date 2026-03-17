package com.tyt.behaviorexample

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hezd.practice.R

/**
 * @author hezd
 * @date 2023/7/13 17:53
 * @description
 */
class TouchExampleActivity :AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_touch_example)
        findViewById<View>(R.id.framelayout).setOnClickListener {
            Log.d("touch","click framelayout")
        }
        findViewById<View>(R.id.buttonWebViewCrash).setOnClickListener {
            Log.d("touch","click button")
        }
    }
}