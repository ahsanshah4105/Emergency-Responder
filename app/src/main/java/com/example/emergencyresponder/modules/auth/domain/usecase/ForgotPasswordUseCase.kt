package com.example.emergencyresponder.modules.auth.domain.usecase

import com.example.emergencyresponder.core.network.AuthException
import com.example.emergencyresponder.core.utils.ValidationUtils
import com.example.emergencyresponder.modules.auth.domain.repository.ForgotPasswordRepository

class ForgotPasswordUseCase(private val repository: ForgotPasswordRepository) {
    suspend operator fun invoke(email: String) {
        if (!ValidationUtils.isEmailValid(email)) {
            throw AuthException.InValidEmailException()
        }
        repository.resetPassword(email)
    }
}