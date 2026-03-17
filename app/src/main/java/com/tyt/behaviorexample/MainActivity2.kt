package com.tyt.behaviorexample

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hezd.practice.R
import com.hezd.practice.databinding.ActivityMain2Binding
//import com.tyt.behaviorexample.ext.init

class MainActivity2 : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMain2Binding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)
        val dependent = findViewById<View>(R.id.dependent)
        println("dependent view id:${dependent.id}")
        println("2 onCreate:thread=${Thread.currentThread()}")
        binding.tvClassloader.text= "object classloader:${Any::class.java.classLoader}"
//        binding.placeholder.init()
//        binding.placeholder.setOnRefreshClickListener {
//            Toast.makeText(this,"Refresh",Toast.LENGTH_SHORT).show()
//        }
    }

    override fun onResume() {
        super.onResume()
        println("2 onResume:thread=${Thread.currentThread()}")
    }
}