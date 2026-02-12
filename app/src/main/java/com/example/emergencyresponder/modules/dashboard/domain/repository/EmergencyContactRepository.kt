package com.example.emergencyresponder.modules.dashboard.domain.repository

import com.example.emergencyresponder.modules.auth.data.model.EmergencyContact

interface EmergencyContactRepository {
    fun observeContacts(uid: String, onUpdate: (List<EmergencyContact>) -> Unit, onError: (String) -> Unit)
    fun addContact(uid: String, contact: EmergencyContact, onResult: (Boolean) -> Unit)

    // Add these two methods:
// Change contactId: String to contact: EmergencyContact
    fun deleteContact(uid: String, contact: EmergencyContact, onResult: (Boolean) -> Unit)
    fun sendSOSToContact(uid: String, contact: EmergencyContact, location: String, onResult: (Boolean) -> Unit)
}
