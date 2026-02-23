package com.example.emergencyresponder.modules.auth.domain.usecase

import com.example.emergencyresponder.core.manager.SPreferenceManager
import com.example.emergencyresponder.modules.auth.domain.repository.ProfileRepository

class ChangeEmailUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(
        currentEmail: String,
        password: String,
        newEmail: String,
        newName: String
    ) {
        repository.requestEmailChange(currentEmail, password, newEmail)
        repository.updateUserProfile(
            uid = SPreferenceManager.getUserId() ?: "",
            name = newName,
            email = newEmail
        )
    }
}
