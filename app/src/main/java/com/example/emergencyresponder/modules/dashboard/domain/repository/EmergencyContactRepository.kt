package com.example.emergencyresponder.modules.dashboard.domain.repository

import com.example.emergencyresponder.modules.auth.data.model.EmergencyContact

interface EmergencyContactRepository {

    fun observeContacts(
        uid: String,
        onUpdate: (List<EmergencyContact>) -> Unit,
        onError: (String) -> Unit
    )

    fun addContact(
        uid: String,
        contact: EmergencyContact,
        onResult: (Boolean) -> Unit
    )
}
