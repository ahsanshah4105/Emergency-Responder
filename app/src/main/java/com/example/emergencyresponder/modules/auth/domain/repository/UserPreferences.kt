package com.example.emergencyresponder.modules.auth.domain.repository

interface UserPreferences {

    fun saveUserSession(uid: String, name: String, email: String)
    fun setUserLoggedIn(isLoggedIn: Boolean)
    fun logoutUser()
}