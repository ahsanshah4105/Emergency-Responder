package com.example.emergencyresponder.core.objects
import android.content.Context
import android.content.SharedPreferences



object SPreferenceManager {

    private const val PREF_NAME = "emergency_prefs"

    // --- APP CONFIG KEYS ---
    private const val KEY_ONBOARDING = "onboarding_completed"
    private const val KEY_LOGIN = "is_logged_in"

    // --- USER SESSION KEYS (New) ---
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_PHONE = "user_phone"
    private const val KEY_USER_UID = "user_uid"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
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

//object SPreferenceManager {
//
//    private const val PREF_NAME = "emergency_prefs"
//    private const val KEY_ONBOARDING = "onboarding_completed"
//    private const val KEY_LOGIN = "is_logged_in"
//
//    private lateinit var prefs: SharedPreferences
//
//    fun init(context: Context) {
//        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//    }
//
//    fun setOnboardingCompleted() {
//        prefs.edit().putBoolean(KEY_ONBOARDING, true).apply()
//    }
//
//    fun isOnboardingCompleted(): Boolean {
//        return prefs.getBoolean(KEY_ONBOARDING, false)
//    }
//
//    fun setUserLoggedIn(value: Boolean) {
//        prefs.edit().putBoolean(KEY_LOGIN, value).apply()
//    }
//
//    fun isUserLoggedIn(): Boolean {
//        return prefs.getBoolean(KEY_LOGIN, false)
//    }
//}
