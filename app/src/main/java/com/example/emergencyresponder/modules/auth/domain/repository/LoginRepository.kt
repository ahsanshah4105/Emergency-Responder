package com.example.emergencyresponder.modules.auth.domain.repository

import com.example.emergencyresponder.modules.auth.data.model.AuthenticatedUser


interface LoginRepository {
    suspend fun login(email: String, password: String): AuthenticatedUser
    suspend fun loginWithGoogle(idToken: String): AuthenticatedUser
}
