package com.hezd.practice.activity

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hezd.practice.databinding.ActivityPermissionCheckBinding
//import com.ymm.lib.permission.MbPermission
//import com.ymm.lib.permission.RequestResult
import timber.log.Timber

/**
 * @author hezd
 * @date 2025/3/24
 * @description
 */
class PermissionCheckActivity : AppCompatActivity() {

    companion object {
        const val TAG = "PermissionCheckActivity"
    }

    private val binding by lazy {
        ActivityPermissionCheckBinding.inflate(LayoutInflater.from(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

//        binding.locationPermission.setOnClickListener {
//
//            MbPermission.with(this)
//                .request(
//                    object : RequestResult {
//                        override fun onGranted(permissions: MutableList<String>?) {
//                            Timber.tag(TAG).d("location permission onGranted")
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                                MbPermission.with(this@PermissionCheckActivity)
//                                    .request(object : RequestResult {
//                                        override fun onGranted(permissions: MutableList<String>?) {
//                                            Timber.tag(TAG).d("background location permission onGranted")
//                                        }
//
//                                        override fun onDenied(
//                                            deniedPerms: MutableList<String>?,
//                                            alwaysDeniedPerms: MutableList<String>?
//                                        ) {
//                                            Timber.tag(TAG).d("background location permission onDenied")
//                                        }
//
//                                    }, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//                            }
//                        }
//
//                        override fun onDenied(
//                            deniedPerms: MutableList<String>?,
//                            alwaysDeniedPerms: MutableList<String>?
//                        ) {
//                            Toast.makeText(
//                                this@PermissionCheckActivity,
//                                "定位授权失败",
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//
//                    },
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                )
//        }
    }
}