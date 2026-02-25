package com.example.emergencyresponder.modules.auth.domain.repository

interface ForgotPasswordRepository {
    suspend fun resetPassword(email: String)
}