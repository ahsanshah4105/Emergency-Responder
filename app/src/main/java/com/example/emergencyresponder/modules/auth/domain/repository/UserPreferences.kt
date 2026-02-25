package com.example.emergencyresponder.modules.auth.domain.repository

interface UserPreferences {

    fun saveUserSession(uid: String, name: String, email: String)

//    fun getUserName(): String?
//    fun getUserEmail(): String?
//    fun getUserId(): String?
//
    fun setUserLoggedIn(isLoggedIn: Boolean)
//    fun isUserLoggedIn(): Boolean
//
//    fun logoutUser()
}