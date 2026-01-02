package com.example.emergencyresponder.modules.auth.domain.useCase

import com.example.emergencyresponder.core.utils.ValidationUtils
import com.example.emergencyresponder.modules.auth.domain.repository.ResetPasswordRepository

class ResetPasswordUseCase(private val repository: ResetPasswordRepository) {
    suspend operator fun invoke(email: String) {
        if (!ValidationUtils.isEmailValid(email)) {
            throw Exception("Please enter a valid email address")
        }
        repository.resetPassword(email)
    }
}