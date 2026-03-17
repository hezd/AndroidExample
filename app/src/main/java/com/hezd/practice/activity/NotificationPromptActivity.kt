package com.hezd.practice.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ToastUtils
import com.hezd.practice.databinding.ActivityNotificationPromptBinding
import com.hjq.permissions.IPermissionInterceptor
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.PermissionFragment
import com.hjq.permissions.XXPermissions

/**
 * @author hezd
 * @date 2025/7/1
 * @description
 */
class NotificationPromptActivity: AppCompatActivity() {
    private val  binding: ActivityNotificationPromptBinding by lazy {
        ActivityNotificationPromptBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnCheckPermission.setOnClickListener {
            checkNotificationPermission()
        }
    }

    private fun checkNotificationPermission() {
        val notificationPermission = Permission.NOTIFICATION_SERVICE
        val granted = XXPermissions.isGranted(this, notificationPermission)
        if(!granted) {
            AlertDialog.Builder(this)
                .setTitle("通知权限已关闭")
                .setMessage("请打开通知，已获取更多货源消息")
                .setNegativeButton("取消"){_,_->

                }
                .setPositiveButton("去开启"){_,_->
                    PermissionFragment.launch(this, arrayListOf(notificationPermission), object :IPermissionInterceptor{}, object :OnPermissionCallback{
                        override fun onGranted(
                            permissions: MutableList<String>,
                            allGranted: Boolean
                        ) {
                            if(allGranted){
                                showToast("已开启")
                            }else {
                                showToast("未开启")
                            }
                        }

                    })
                }
                .show()
        }else{
            showToast("已开启")
        }
    }

    private fun showToast(message:String){
        Toast.makeText(this,message,Toast.LENGTH_LONG).show()
    }
}