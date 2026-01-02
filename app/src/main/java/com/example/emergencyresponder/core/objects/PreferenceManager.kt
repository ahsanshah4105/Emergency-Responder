package com.example.emergencyresponder.core.objects
import android.content.Context
import android.content.SharedPreferences

object SPreferenceManager {

    private const val PREF_NAME = "emergency_prefs"
    private const val KEY_ONBOARDING = "onboarding_completed"
    private const val KEY_LOGIN = "is_logged_in"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_ONBOARDING, true).apply()
    }

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING, false)
    }

    fun setUserLoggedIn(value: Boolean) {
        prefs.edit().putBoolean(KEY_LOGIN, value).apply()
    }

    fun isUserLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_LOGIN, false)
    }
}
