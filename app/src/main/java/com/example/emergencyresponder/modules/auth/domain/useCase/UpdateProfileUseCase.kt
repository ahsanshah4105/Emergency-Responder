package com.example.emergencyresponder.modules.auth.domain.useCase

import com.example.emergencyresponder.modules.auth.domain.repository.ProfileRepository


class UpdateProfileUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(uid: String, name: String, phone: String) {
        if (name.isBlank()) throw Exception("Name cannot be empty")
        // Add more business rules here if needed (e.g., validate phone)

        repository.updateUserProfile(uid, name, phone)
    }
}