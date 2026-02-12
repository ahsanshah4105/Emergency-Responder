package com.example.emergencyresponder.modules.auth.domain.repository


interface ProfileRepository {
    suspend fun updateUserProfile(uid: String, name: String, email: String)
    suspend fun requestEmailChange(
        currentEmail: String,
        password: String,
        newEmail: String
    )

}