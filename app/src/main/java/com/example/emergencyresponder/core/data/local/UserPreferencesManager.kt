package com.example.emergencyresponder.core.data.local

import com.example.emergencyresponder.core.common.PrefKeys
import com.example.emergencyresponder.core.domain.repository.IBasePreference

class UserPreferencesManager(private val prefs: IBasePreference) {

    fun setLoggedIn(isLoggedIn: Boolean) = prefs.saveBoolean(PrefKeys.IS_LOGGED_IN, isLoggedIn)
    fun isLoggedIn() = prefs.getBoolean(PrefKeys.IS_LOGGED_IN)

    fun setUserName(name: String?) = prefs.saveString(PrefKeys.USER_NAME, name)
    fun getUserName() = prefs.getString(PrefKeys.USER_NAME, "User")

    fun clearSession() = prefs.clearUserSession()
}