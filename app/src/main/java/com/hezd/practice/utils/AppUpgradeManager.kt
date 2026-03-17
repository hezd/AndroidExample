package com.hezd.practice.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * @author hezd
 * @date 2025/7/26
 * @description 应用商店更新管理类
 */
object AppUpgradeManager {
    /**
     * 打开应用上带你
     * @param context 上下文
     * @param packageName 应用包名
     * @param backupUrl 兜底的备用app下载地址
     */
    @JvmStatic
    fun startStore(context: Context, packageName: String, backupUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            intent.setData(Uri.parse("market://details?id=$packageName"))
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                if (context !is Activity) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                intent.setData(Uri.parse(backupUrl))
                context.startActivity(intent)
            } catch (ignored: Exception) {
            }
        }
    }

    @JvmStatic
    fun startDownload(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            intent.setData(Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
        }
    }
}