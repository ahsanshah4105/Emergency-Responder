package com.example.emergencyresponder.modules.dashboard.data.repositoryImpl

import com.example.emergencyresponder.core.domain.model.EmergencyContact
import com.example.emergencyresponder.modules.dashboard.data.datasource.EmergencyContactRemoteDataSource
import com.example.emergencyresponder.modules.dashboard.data.mapper.DashboardMapper.toData
import com.example.emergencyresponder.modules.dashboard.data.mapper.DashboardMapper.toDomain
import com.example.emergencyresponder.modules.dashboard.domain.repository.IEmergencyContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EmergencyContactRepositoryImpl(
    private val remote: EmergencyContactRemoteDataSource
) : IEmergencyContactRepository {

    override fun observeContacts(uid: String): Flow<List<EmergencyContact>> {
        return remote.observeContacts(uid).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addContact(uid: String, contact: EmergencyContact) {
        remote.addContact(uid, contact.toData())
    }

    override suspend fun deleteContact(uid: String, contact: EmergencyContact) {
        remote.deleteContact(uid, contact.toData())
    }

    override suspend fun sendSOSToContact(
        uid: String,
        contact: EmergencyContact,
        location: String
    ) {
        remote.sendSOSNotification(uid, contact.toData(), location)
    }
}