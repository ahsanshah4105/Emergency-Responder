package com.example.emergencyresponder.modules.dashboard.data.repositoryImpl

import com.example.emergencyresponder.modules.auth.data.model.EmergencyContact
import com.example.emergencyresponder.modules.dashboard.data.datasource.EmergencyContactRemoteDataSource
import com.example.emergencyresponder.modules.dashboard.domain.repository.IEmergencyContactRepository
import kotlinx.coroutines.flow.Flow

class EmergencyContactRepositoryImpl(
    private val remote: EmergencyContactRemoteDataSource
) : IEmergencyContactRepository {

    override fun observeContacts(uid: String): Flow<List<EmergencyContact>> {
        return remote.observeContacts(uid)
    }

    override suspend fun addContact(uid: String, contact: EmergencyContact) {
        remote.addContact(uid, contact)
    }

    override suspend fun deleteContact(uid: String, contact: EmergencyContact) {
        remote.deleteContact(uid, contact)
    }

    override suspend fun sendSOSToContact(
        uid: String,
        contact: EmergencyContact,
        location: String
    ) {
        remote.sendSOSNotification(uid, contact, location)
    }
}