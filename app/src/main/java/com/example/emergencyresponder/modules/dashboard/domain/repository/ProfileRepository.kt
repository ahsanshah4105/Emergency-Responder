package com.example.emergencyresponder.modules.dashboard.domain.repository

interface ProfileRepository {
    suspend fun updateUserProfile(uid: String, name: String, email: String)
    suspend fun requestEmailChange(
        currentEmail: String, password: String, newEmail: String
    )
}

interface IProfileRepository {
    suspend fun updateProfile(uid: String, name: String, email: String)
    suspend fun changeEmail(password: String, newEmail: String)
    suspend fun updatePassword(currentPassword: String, newPassword: String)
}