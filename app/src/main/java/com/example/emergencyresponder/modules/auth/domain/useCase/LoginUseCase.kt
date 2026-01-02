package com.example.emergencyresponder.modules.auth.domain.useCase

import com.example.emergencyresponder.modules.auth.domain.repository.LoginRepository

class LoginUseCase(
    private val repository: LoginRepository
) {
    suspend operator fun invoke(email: String, password: String) {
        val user = repository.login(email, password)
        if (!user.isEmailVerified) {
            throw Exception("Email not verified. Please check your email.")
        }
    }
}