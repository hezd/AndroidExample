package com.hezd.practice.activity

import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hezd.practice.databinding.ActivityEventDispatchBinding
import java.io.File
import java.io.IOException

/**
 * 面试题：如果一个RelativeLayout包裹了一个Button，他们都监听click事件，
 * 当手指点击button然后移动出Button区域后松开手指，
 *
 * 理解源码我认为还是有一点小技巧的，就是只关心主流程忽略一些细节否则会陷入细节无法自拔，什么是主流程是指跟我们关注点相关的流程
 * 可以结合断点调试的方式来理解源码
 */
class EventDispatchActivity : AppCompatActivity() {

    companion object {
        const val TAG = "dispatch"
    }

    private lateinit var binding: ActivityEventDispatchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEventDispatchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.button.setOnClickListener {
            showToast("button click!")
            getExternalStorageDirectoryTest()
        }

        binding.relativeLayout.setOnClickListener {
            showToast("relative layout click!")
        }
    }

    private fun getExternalStorageDirectoryTest() {
//        val externalPath = Environment.getExternalStorageDirectory().absolutePath
        val externalPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
        val testFilePath = externalPath + File.separator + "test.txt"
        val testFile = File(testFilePath)
        if (!testFile.exists()) {
            try {
                println("文件不存在，开始创建")
                val createResult = testFile.createNewFile()
                println("文件创建${if (createResult) "成功" else "失败"}")
            } catch (e: IOException) {
                    e.printStackTrace()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this@EventDispatchActivity, message, Toast.LENGTH_SHORT).show()
    }

}