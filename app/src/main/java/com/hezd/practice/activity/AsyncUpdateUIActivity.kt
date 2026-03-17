package com.hezd.practice.activity

import android.os.Bundle
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.hezd.practice.R
import com.hezd.practice.databinding.ActivityAnsycUpdateUiactivityBinding
import kotlin.concurrent.thread

/**
 * 异步更新UI调试
 */
class AsyncUpdateUIActivity : AppCompatActivity() {
    private val textView by lazy {
        TextView(this).apply {
            setBackgroundColor(resources.getColor(R.color.black))
            setTextColor(resources.getColor(R.color.white))
            text = "111"
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAnsycUpdateUiactivityBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)

//        Thread {
//            println("222")
//            binding.textview.text = "222"
//        }.start()

        thread {
            Thread.sleep(1000)
            Looper.prepare()
            showDialog()
            textView.postDelayed({
                Thread.sleep(2000)
                textView.text = "222"
            },2000)

            Looper.loop()

        }

//        showDialog()
    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(this)

        builder.setView(textView)
        builder.setCancelable(true)
        builder.create().show()
    }
}