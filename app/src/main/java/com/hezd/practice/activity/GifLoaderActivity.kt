package com.hezd.practice.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.hezd.practice.R
import com.hezd.practice.databinding.ActivityGifLoaderBinding

/**
 * @author hezd
 * @date 2024/12/2
 * @description
 */
class GifLoaderActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityGifLoaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Glide.with(this)
            .asGif()
            .load(R.mipmap.icon_search_gif)
            .into(binding.imageSearch)
    }
}