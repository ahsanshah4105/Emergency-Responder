package com.example.emergencyresponder.modules.auth.domain.repository

import com.example.emergencyresponder.modules.auth.domain.model.User

interface SignUpRepository {
    suspend fun signUp(email: String, password: String, user: User)
}