package com.example.emergencyresponder.modules.auth.domain.usecase

import com.example.emergencyresponder.modules.auth.domain.model.AuthenticatedUser
import com.example.emergencyresponder.modules.auth.domain.repository.LoginRepository

class LoginUseCase(
    private val repository: LoginRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthenticatedUser {
        return repository.login(email, password)
    }

    suspend fun executeGoogleLogin(idToken: String): AuthenticatedUser {
        return repository.loginWithGoogle(idToken)
    }
}