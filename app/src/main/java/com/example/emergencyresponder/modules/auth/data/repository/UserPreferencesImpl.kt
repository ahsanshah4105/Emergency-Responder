package com.example.emergencyresponder.modules.auth.data.repository

import com.example.emergencyresponder.core.manager.SPreferenceManager
import com.example.emergencyresponder.modules.auth.domain.repository.UserPreferences

class UserPreferencesImpl : UserPreferences {

    override fun saveUserSession(uid: String, name: String, email: String) {
        SPreferenceManager.saveUserSession(uid, name, email)
    }

    override fun setUserLoggedIn(isLoggedIn: Boolean) {
        SPreferenceManager.setUserLoggedIn(isLoggedIn)
    }

    override fun logoutUser() {
        SPreferenceManager.logoutUser()
    }
}