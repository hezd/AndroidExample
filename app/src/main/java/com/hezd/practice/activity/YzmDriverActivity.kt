package com.hezd.practice.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.hezd.practice.databinding.ActivityYzmDriverBinding
import com.hezd.practice.utils.AppUpgradeManager


/**
 * @author hezd
 * @date 2026/2/9
 * @description
 */
class YzmDriverActivity : AppCompatActivity() {

    lateinit var binding: ActivityYzmDriverBinding

    companion object{
        const val downloadUrl = "https://qhy.56yzm.com/h5/driverApp/index.html"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYzmDriverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonWakeUp.setOnClickListener {
            if (isInstalledApp(this, "com.yzm56.driver")) {
                startUpYzmDriver()
            } else {
                AppUpgradeManager.startDownload(this, downloadUrl)
            }
        }
    }

    private fun startUpYzmDriver() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse("yzmdriver://mapp.56yzm.com"))
        startActivity(intent)
    }

    fun isInstalledApp(context: Context, packageName: String?): Boolean {
        if (TextUtils.isEmpty(packageName)) {
            return false
        } else {
            try {
                val packageInfo = context.packageManager.getPackageInfo(
                    packageName!!, 0
                )
                return packageInfo != null
            } catch (e: PackageManager.NameNotFoundException) {
                return false
            }
        }
    }
}