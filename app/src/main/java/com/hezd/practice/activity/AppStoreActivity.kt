package com.hezd.practice.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hezd.practice.databinding.ActivityAppStoreBinding

/**
 * @author hezd
 * @date 2025/4/14
 * @description
 */
class AppStoreActivity : AppCompatActivity() {

    companion object {
        const val PACKAGE_NAME = "com.tyt.huozhan"
    }

    lateinit var binding: ActivityAppStoreBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppStoreBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.btnAppStore.setOnClickListener {
//            startAppStore(this, PACKAGE_NAME, success = {}, error = {
//                Toast.makeText(this, "打开应用商店失败", Toast.LENGTH_SHORT).show()
//            })
            startAppStore()
        }
    }

    private fun startAppStore(
        context: Context,
        packageName: String,
        success: () -> Unit,
        error: (Exception) -> Unit
    ) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            intent.setData(Uri.parse("market://details?id=$packageName"))
            context.startActivity(intent)
            success.invoke()
        } catch (e: Exception) {
            error.invoke(e)
        }
    }

    private fun startAppStore() {
        val packageName = "com.tyt.huozhan"

        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // 白名单（官方商店）
            val officialStores = setOf(
                "com.huawei.appmarket",       // 华为
                "com.hihonor.appmarket",      // 荣耀
                "com.sec.android.app.samsungapps", // 三星
                "com.xiaomi.market",          // 小米
                "com.heytap.market",            // OPPO
                "com.vivo.market",            // vivo
                "com.tencent.android.qqdownloader", // 应用宝
            )

            val pm = packageManager
            val list = pm.queryIntentActivities(intent, 0)
            var found = false

            for (info in list) {
                val pkn = info.activityInfo.packageName
                if (officialStores.contains(pkn)) {
                    intent.setPackage(pkn) // 强制只跳官方商店
                    found = true
                    break
                }
            }

            if (found) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "未安装官方应用商店", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "跳转失败", Toast.LENGTH_SHORT).show()
        }
    }

//    private fun fetchPackageName(): String {
//        if(RomUtils.isOppo()){
//
//        }
//    }

//    private fun isAppStoreInstalled():Boolean{
//        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$PACKAGE_NAME"))
//    }
}