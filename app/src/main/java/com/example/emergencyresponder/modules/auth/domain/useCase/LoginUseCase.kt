package com.example.emergencyresponder.modules.auth.domain.useCase

import com.example.emergencyresponder.modules.auth.domain.service.LoginRepository

class LoginUseCase(
    private val repository: LoginRepository
) {
    suspend operator fun invoke(email: String, password: String) {
        repository.login(email, password)
    }
}