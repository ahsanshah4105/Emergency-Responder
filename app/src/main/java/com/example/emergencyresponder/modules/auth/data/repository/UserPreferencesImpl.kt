package com.example.emergencyresponder.modules.auth.data.repository

import com.example.emergencyresponder.core.manager.SPreferenceManager
import com.example.emergencyresponder.modules.auth.domain.repository.UserPreferences

class UserPreferencesImpl : UserPreferences {

    override fun saveUserSession(uid: String, name: String, email: String) {
        SPreferenceManager.saveUserSession(uid, name, email)
    }

//    override fun getUserName(): String? {
//        return SPreferenceManager.getUserName()
//    }
//
//    override fun getUserEmail(): String? {
//        return SPreferenceManager.getUserEmail()
//    }
//
//    override fun getUserId(): String? {
//        return SPreferenceManager.getUserId()
//    }
//
    override fun setUserLoggedIn(isLoggedIn: Boolean) {
       SPreferenceManager.setUserLoggedIn(isLoggedIn)
    }
//
//    override fun isUserLoggedIn(): Boolean {
//        return SPreferenceManager.isUserLoggedIn(false)
//    }
//
//    override fun logoutUser() {
//        SPreferenceManager.logoutUser()
//    }
}