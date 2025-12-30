package com.example.emergencyresponder.modules.auth.domain.service

interface ResetPasswordRepository {
    suspend fun resetPassword(email: String)
}