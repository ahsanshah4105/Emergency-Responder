package com.example.emergencyresponder.core.manager
import android.content.Context
import android.content.SharedPreferences



object SPreferenceManager {

    private const val PREF_NAME = "emergency_prefs"

    // --- APP CONFIG KEYS ---
    private const val KEY_ONBOARDING = "onboarding_completed"
    private const val KEY_LOGIN = "is_logged_in"

    private const val Sensitivity_COUNT = "count"

    // --- USER SESSION KEYS (New) ---
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_PHONE = "user_phone"
    private const val KEY_USER_UID = "user_uid"
    private const val KEY_CANCEL_COUNT = "cancel_count_monitor"
    private const val KEY_SENSITIVITY = "app_sensitivity_level"
    private lateinit var prefs: SharedPreferences
    private const val KEY_BATTERY_ASKED = "battery_optimization_asked"
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    fun getSensitivity(): String {
        return prefs.getString(KEY_SENSITIVITY, "MEDIUM") ?: "MEDIUM"
    }

    fun setSensitivity(level: String) {
        prefs.edit().putString(KEY_SENSITIVITY, level).apply()
    }
    fun incrementCancelCount() {
        val current = prefs.getInt(KEY_CANCEL_COUNT, 0)
        prefs.edit().putInt(KEY_CANCEL_COUNT, current + 1).apply()
    }

    fun getCancelCount(): Int {
        return prefs.getInt(KEY_CANCEL_COUNT, 0)
    }

    fun resetCancelCount() {
        prefs.edit().putInt(KEY_CANCEL_COUNT, 0).apply()
    }
    // --- EXISTING APP CONFIG ---
    fun setOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_ONBOARDING, true).apply()
    }

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING, false)
    }

    fun isUserLoggedIn(bool: Boolean): Boolean {
        return prefs.getBoolean(KEY_LOGIN, false)
    }
    fun setUserLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_LOGIN, isLoggedIn).apply()
    }
    fun setBatteryAsked(asked: Boolean) {
        prefs.edit().putBoolean(KEY_BATTERY_ASKED, asked).apply()
    }

    /**
     * Checks if the user has already been prompted about battery optimization.
     */
    fun hasAskedBattery(): Boolean {
        return prefs.getBoolean(KEY_BATTERY_ASKED, false)
    }
    fun setUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }
    fun setCounter(int: Int) {
        prefs.edit().putInt(Sensitivity_COUNT, int).apply()

    }
    // --- NEW: SAVE USER SESSION (Login/Signup Success par call krein) ---
    fun saveUserSession(uid: String, name: String, email: String) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_LOGIN, true)
        editor.putString(KEY_USER_UID, uid)
        editor.putString(KEY_USER_NAME, name)
        editor.putString(KEY_USER_EMAIL, email)
        editor.apply()
    }

    // --- NEW: GET USER DETAILS (Profile/Dashboard par use krein) ---
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, "User")
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, "")
    fun getUserPhone(): String? = prefs.getString(KEY_USER_PHONE, "")
    fun getUserId(): String? = prefs.getString(KEY_USER_UID, "")

    fun getCounter(): Int = prefs.getInt(Sensitivity_COUNT, 0)

    // --- NEW: LOGOUT (Ye sab se important hai) ---
    fun logoutUser() {
        val editor = prefs.edit()

        // Sirf User ka data clear krein
        editor.remove(KEY_LOGIN)
        editor.remove(KEY_USER_UID)
        editor.remove(KEY_USER_NAME)
        editor.remove(KEY_USER_EMAIL)
        editor.remove(KEY_USER_PHONE)

        // NOTE: Hum 'KEY_ONBOARDING' ko remove nahi kr rahe.
        // Taake logout k baad user wapis Login screen par jaye, Onboarding par nahi.

        editor.apply()
    }
}