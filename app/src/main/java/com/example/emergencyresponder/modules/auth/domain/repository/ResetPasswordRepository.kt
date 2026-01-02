package com.example.emergencyresponder.modules.auth.domain.repository

interface ResetPasswordRepository {
    suspend fun resetPassword(email: String)
}