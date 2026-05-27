package com.example.emergencyresponder.modules.dashboard.domain.repository

import com.example.emergencyresponder.core.domain.model.EmergencyContact
import kotlinx.coroutines.flow.Flow

interface IEmergencyContactRepository {
    fun observeContacts(uid: String): Flow<List<EmergencyContact>>
    suspend fun addContact(uid: String, contact: EmergencyContact)
    suspend fun deleteContact(uid: String, contact: EmergencyContact)
    suspend fun sendSOSToContact(uid: String, contact: EmergencyContact, location: String)
}
