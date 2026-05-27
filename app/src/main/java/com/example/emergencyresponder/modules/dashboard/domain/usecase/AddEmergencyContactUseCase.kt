package com.example.emergencyresponder.modules.dashboard.domain.usecase

import com.example.emergencyresponder.core.network.AuthException
import com.example.emergencyresponder.core.utils.ValidationUtils
import com.example.emergencyresponder.core.domain.model.EmergencyContact
import com.example.emergencyresponder.modules.dashboard.domain.repository.IEmergencyContactRepository

class AddEmergencyContactUseCase(
    private val repository: IEmergencyContactRepository
) {
    suspend operator fun invoke(
        uid: String, contact: EmergencyContact
    ) {
        if (contact.name.isBlank()) throw AuthException.NameCannotBeEmpty()
        if (!ValidationUtils.isPhoneValid(contact.phone)) {
            throw AuthException.InvalidPhoneFormat()
        }
        repository.addContact(uid, contact)
    }
}
