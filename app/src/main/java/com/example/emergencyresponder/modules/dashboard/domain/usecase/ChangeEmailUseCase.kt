package com.example.emergencyresponder.modules.dashboard.domain.usecase

import com.example.emergencyresponder.core.network.AuthException
import com.example.emergencyresponder.modules.dashboard.domain.repository.IProfileRepository

class ChangeEmailUseCase(
    private val repository: IProfileRepository
) {
    suspend operator fun invoke(
        password: String,
        newEmail: String,
        newName: String,
        uid: String
    ) {
        if (newEmail.isBlank()) throw AuthException.NewEmailRequiredException()
        if (password.isBlank()) throw AuthException.CurrentPasswordRequiredException()
        repository.changeEmail(password, newEmail)
        repository.updateProfile(uid, newName, newEmail)
    }
}