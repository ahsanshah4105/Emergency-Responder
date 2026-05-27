package com.example.emergencyresponder.modules.auth.domain.usecase

import com.example.emergencyresponder.modules.auth.domain.model.User
import com.example.emergencyresponder.modules.auth.domain.repository.SignUpRepository

class SignUpUseCase(
    private val repository: SignUpRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        user: User
    ) {
        repository.signUp(email, password, user)
    }
}