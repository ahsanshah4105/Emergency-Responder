package com.example.emergencyresponder.modules.dashboard.domain.usecase

import com.example.emergencyresponder.modules.auth.data.model.EmergencyContact
import com.example.emergencyresponder.modules.dashboard.domain.repository.IEmergencyContactRepository

class DeleteEmergencyContactUseCase(
    private val repository: IEmergencyContactRepository
) {
    suspend operator fun invoke(uid: String, contact: EmergencyContact) {
        repository.deleteContact(uid, contact)
    }
}