package com.example.emergencyresponder.core.data.local

import android.content.Context
import com.example.emergencyresponder.core.domain.repository.IBasePreference
import androidx.core.content.edit


class PreferenceProviderImpl(context: Context) : IBasePreference {
    private val prefs = context.getSharedPreferences("emergency_prefs", Context.MODE_PRIVATE)
    companion object {
        private const val KEY_ONBOARDING = "onboarding_completed"
        private const val KEY_LOGIN = "is_logged_in"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_UID = "user_uid"
        private const val KEY_CANCEL_COUNT = "cancel_count_monitor"
        private const val KEY_SENSITIVITY = "app_sensitivity_level"
        private const val KEY_BATTERY_ASKED = "battery_optimization_asked"
    }

    override fun saveString(key: String, value: String?) = prefs.edit { putString(key, value) }
    override fun getString(key: String, default: String) = prefs.getString(key, default) ?: default
    override fun saveBoolean(key: String, value: Boolean) = prefs.edit { putBoolean(key, value) }
    override fun getBoolean(key: String, default: Boolean) = prefs.getBoolean(key, default)
    override fun saveInt(key: String, value: Int) = prefs.edit { putInt(key, value) }
    override fun getInt(key: String, default: Int) = prefs.getInt(key, default)

    override fun getSensitivity() = getString(KEY_SENSITIVITY, "MEDIUM")
    override fun setSensitivity(level: String) = saveString(KEY_SENSITIVITY, level)

    override fun incrementCancelCount() {
        val current = getInt(KEY_CANCEL_COUNT, 0)
        saveInt(KEY_CANCEL_COUNT, current + 1)
    }

    override fun getCancelCount() = getInt(KEY_CANCEL_COUNT, 0)
    override fun resetCancelCount() = saveInt(KEY_CANCEL_COUNT, 0)

    override fun isOnboardingCompleted() = getBoolean(KEY_ONBOARDING, false)
    override fun setOnboardingCompleted() = saveBoolean(KEY_ONBOARDING, true)

    override fun isUserLoggedIn() = getBoolean(KEY_LOGIN, false)
    override fun setUserLoggedIn(isLoggedIn: Boolean) = saveBoolean(KEY_LOGIN, isLoggedIn)

    override fun hasAskedBattery() = getBoolean(KEY_BATTERY_ASKED, false)
    override fun setBatteryAsked(asked: Boolean) = saveBoolean(KEY_BATTERY_ASKED, asked)
    override fun saveUserSession(uid: String, name: String, email: String) {
        prefs.edit().apply {
            putBoolean(KEY_LOGIN, true)
            putString(KEY_USER_UID, uid)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
        }.apply()
    }

    override fun clearUserSession() {
        prefs.edit().apply {
            remove(KEY_LOGIN)
            remove(KEY_USER_UID)
            remove(KEY_USER_NAME)
            remove(KEY_USER_EMAIL)
        }.apply()
    }

}