package com.example.emergencyresponder.modules.auth.domain.useCase

import com.example.emergencyresponder.core.objects.SPreferenceManager
import com.example.emergencyresponder.modules.auth.domain.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth

class ChangeEmailUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(
        password: String,
        newEmail: String,
        newName: String
    ) {
        val currentEmail = FirebaseAuth.getInstance().currentUser?.email
            ?: throw Exception("User not logged in")

        repository.requestEmailChange(currentEmail, password, newEmail)
        repository.updateUserProfile(
            uid = SPreferenceManager.getUserId() ?: "",
            name = newName,
            email = newEmail
        )
    }

}
