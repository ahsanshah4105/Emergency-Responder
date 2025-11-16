package com.example.emergencyresponder.core.objects
import android.content.Context
import android.content.SharedPreferences

object SPreferenceManager {

    private const val PREF_NAME = "app_preferences"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
    }

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false)
    }
}
