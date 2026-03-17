package com.hezd.practice.bitmap

import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.hezd.practice.R
import com.hezd.practice.databinding.ActivityBitmapBinding
import com.tyt.behaviorexample.MainActivity

class BitmapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityBitmapBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)
        val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.icon_launch_failed)
        val size = 452 * 198 * 4
        val dpi = Resources.getSystem().displayMetrics.densityDpi
        val scale = dpi / 320f
        val realSize = 452 * scale * 198 * scale * 4
        Log.d("bitmap", "screen dpi=$dpi")
        Log.d("bitmap", "screen density=${Resources.getSystem().displayMetrics.density}")
        Log.d("bitmap", "dpi scale=$scale")
        Log.d("bitmap", "bitmap standard size: 452x198x4=$size")
        Log.d("bitmap", "bitmap size real: ((452xscale)x(198xscale)x4)=$realSize")
        Log.d("bitmap", "bitmap allocate size=${bitmap.allocationByteCount}")

//        binding.textview.text = "image size :${bitmap.allocationByteCount}"
    }
}