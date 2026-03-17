//package com.tyt.behaviorexample.ext
//
//import android.content.res.Resources
//import android.graphics.Color
//import android.graphics.Typeface
//import android.util.TypedValue
//import android.view.Gravity
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.TextView
//import com.hezd.practice.R
//import com.ymm.lib.widget.placeholder.MBPlaceholderView
//
///**
// * @author hezd
// * @date 2023/7/4 17:35
// * @description
// */
//val Float.dp
//get() = (Resources.getSystem().displayMetrics.density*this+0.5f).toInt()
//
//val Int.dp
//get() = (Resources.getSystem().displayMetrics.density*this+0.5f).toInt()
//
//fun MBPlaceholderView.init(){
//    val childLayout = getChildAt(0)
//    if(childLayout is LinearLayout){
//        childLayout.gravity = Gravity.CENTER
//    }
//    val image = findViewById<ImageView>(com.ymm.abnormalpage.R.id.image)
//    image.layoutParams.height = 66.dp
//    image.layoutParams.width = 151.dp
//
//    val titleTv = findViewById<TextView>(com.ymm.abnormalpage.R.id.tv_title)
//    titleTv.setTextColor(Color.parseColor("#ff333333"))
//    titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP,18f)
//    titleTv.setPadding(0,15.dp,0,0)
//    titleTv.includeFontPadding = false
//    titleTv.text = "网络竟然崩溃了"
//
//
//    val contentTv = findViewById<TextView>(com.ymm.abnormalpage.R.id.tv_content)
//    val contentLayoutParams = contentTv.layoutParams
//    if(contentLayoutParams is LinearLayout.LayoutParams){
//        contentLayoutParams.topMargin = 10.dp
//    }
//    contentTv.includeFontPadding = false
//    contentTv.setTextColor(Color.parseColor("#ff999999"))
//    contentTv.setTextSize(TypedValue.COMPLEX_UNIT_SP,15f)
//
//    val refreshTv = findViewById<TextView>(com.ymm.abnormalpage.R.id.tv_action_refresh)
//    val refreshLayoutParams = refreshTv.layoutParams
//    if(refreshLayoutParams is LinearLayout.LayoutParams){
//        refreshLayoutParams.topMargin = 20.dp
//    }
//    refreshTv.setBackgroundResource(R.drawable.refresh_button_round_boarder)
//    refreshTv.setTextColor(Color.parseColor("#ff333333"))
//    refreshTv.setTextSize(TypedValue.COMPLEX_UNIT_SP,14f)
//    refreshTv.typeface = Typeface.DEFAULT
//    refreshTv.text = "刷新"
//
//}
