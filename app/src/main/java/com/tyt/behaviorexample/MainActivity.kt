package com.tyt.behaviorexample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import com.hezd.practice.R
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println("onCreate:thread=${Thread.currentThread()}")
        val dependentView = findViewById<View>(R.id.dependent)
        println("dependent view id:${dependentView.id}")
        val toolbar = findViewById<View>(R.id.barview)
        val titleLayout = findViewById<ConstraintLayout>(R.id.toolbar)
        val titleTv = findViewById<TextView>(R.id.tv_title)
        // 初始顶部距离
        var originalY = 0
        // 头像初始高度
        var originalH = 0
        // 标题栏高度
        var titleH = 0
        titleLayout.post {
            titleH = titleLayout.height
        }
        dependentView.post {
            originalY = dependentView.top
            originalH = dependentView.height
        }
        val follow = findViewById<View>(R.id.follow)
        follow.pivotX = 0f
        follow.pivotY = 0f

        val scrollView = findViewById<NestedScrollView>(R.id.scrollView)
        scrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            val realY = abs(scrollY)
            val maxHeight = originalY+(titleH-originalH)
            Log.d("behavior", "=====>scrollY=$scrollY,maxHeight=$maxHeight")
            if(realY<maxHeight) {
                titleTv.alpha=0f
                var scale = realY*1.0f/maxHeight
                if(scale>1) scale = 1f
                follow.scaleX = 1-scale*0.5f
                follow.scaleY = 1-scale*0.5f
                follow.translationY = -scrollY.toFloat()
                toolbar.alpha = scale
            }else{
                titleTv.alpha = 1f
                follow.translationY = -maxHeight.toFloat()
                follow.scaleX = 0.5f
                follow.scaleY = 0.5f
            }
        }

//        dependentView.setOnClickListener {
//            ViewCompat.offsetTopAndBottom(it,20)
//        }
    }

    override fun onResume() {
        super.onResume()
        println("onResume:thread=${Thread.currentThread()}")

//        startActivity(Intent(this,TouchExampleActivity::class.java))
    }
}