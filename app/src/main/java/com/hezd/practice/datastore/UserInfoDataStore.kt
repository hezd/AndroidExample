package com.hezd.practice.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * @author hezd
 * @date 2023/9/26 09:38
 * @description
 */

const val SP_NAME = "sharedPreferences"
const val KEY_NAME = "sp_name"
const val KEY_USER = "user"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SP_NAME,
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, SP_NAME))
    })

class UserInfoDataStore(dataStore: DataStore<Preferences>) {
    private val name = stringPreferencesKey(KEY_NAME)

    val namePreferences:Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {preferences->
            preferences[name]?:""
        }

}

data class UserInfo(val name:String)

