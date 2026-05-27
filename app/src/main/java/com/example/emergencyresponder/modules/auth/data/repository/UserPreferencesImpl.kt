package com.example.emergencyresponder.modules.auth.data.repository

import com.example.emergencyresponder.core.domain.repository.IBasePreference
import com.example.emergencyresponder.modules.auth.domain.repository.UserPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesImpl @Inject constructor(
    private val prefs: IBasePreference
) : UserPreferences {

    override fun saveUserSession(uid: String, name: String, email: String) {
        prefs.saveUserSession(uid, name, email)
    }

    override fun setUserLoggedIn(isLoggedIn: Boolean) {
        prefs.setUserLoggedIn(isLoggedIn)
    }

    override fun logoutUser() {
        prefs.clearUserSession()
    }
}