package com.example.emergencyresponder.modules.dashboard.domain.usecase

import com.example.emergencyresponder.core.network.AuthException
import com.example.emergencyresponder.modules.dashboard.domain.repository.IProfileRepository

class UpdateProfileUseCase(
    private val repository: IProfileRepository
) {
    suspend operator fun invoke(uid: String, name: String, email: String) {
        if (name.isBlank()) throw AuthException.NameCannotBeEmpty()
        if (name.length < 3) throw AuthException.NameIsTooShort()
        repository.updateProfile(uid, name, email)
    }
}