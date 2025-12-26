package com.example.emergencyresponder.modules.auth.domain.service


interface LoginRepository {
    suspend fun login(email: String, password: String)
}
