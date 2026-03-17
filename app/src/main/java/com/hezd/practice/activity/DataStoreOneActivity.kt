package com.hezd.practice.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hezd.practice.databinding.ActivityDataStoreOneBinding
import com.hezd.practice.datastore.KEY_NAME
import com.hezd.practice.datastore.SP_NAME
import com.hezd.practice.datastore.UserInfoDataStore
import com.hezd.practice.datastore.dataStore
import com.hezd.practice.utils.SharedPreferencesUtils
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 测试SharePreferences迁移到DataStore
 */
class DataStoreOneActivity : AppCompatActivity() {
    companion object {
        const val TAG = "DataStoreOneActivity"

    }

    private lateinit var sharedPreferences: SharedPreferences
    private val userInfoDataStore:UserInfoDataStore by lazy { UserInfoDataStore(dataStore) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDataStoreOneBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)

        testSharePreference()
        testDataStore()
    }

    private fun testDataStore() {
        lifecycleScope.launch {
            userInfoDataStore.namePreferences.collect {
                Timber.tag(TAG).e("get name by data store:$it")
            }
        }

    }

    private fun testSharePreference() {
        sharedPreferences = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
//        SharedPreferencesUtils.get
        val edit = sharedPreferences.edit()
        val value = "hezd"
        Timber.tag(TAG).e("set name by sharedPreferences ,value:$value")
        edit.putString(KEY_NAME, value)
        edit.apply()
        val nameValue = sharedPreferences.getString(KEY_NAME, "")
        Timber.tag(TAG).e("get name by sharedPreferences value:%s", nameValue)
    }
}