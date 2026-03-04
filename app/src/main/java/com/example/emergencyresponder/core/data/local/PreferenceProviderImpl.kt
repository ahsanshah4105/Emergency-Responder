package com.example.emergencyresponder.core.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.emergencyresponder.core.domain.repository.IBasePreference

class PreferenceProviderImpl(context: Context) : IBasePreference {

    private val prefs: SharedPreferences = context.getSharedPreferences("emergency_prefs", Context.MODE_PRIVATE)

    override fun saveString(key: String, value: String?) = prefs.edit().putString(key, value).apply()
    override fun getString(key: String, default: String): String = prefs.getString(key, default) ?: default

    override fun saveBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
    override fun getBoolean(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)

    override fun saveInt(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
    override fun getInt(key: String, default: Int): Int = prefs.getInt(key, default)

    override fun remove(key: String) = prefs.edit().remove(key).apply()

    override fun clearUserSession() {
        prefs.edit().apply {
            remove("is_logged_in")
            remove("user_uid")
            remove("user_name")
            remove("user_email")
            remove("user_phone")
        }.apply()
    }


}