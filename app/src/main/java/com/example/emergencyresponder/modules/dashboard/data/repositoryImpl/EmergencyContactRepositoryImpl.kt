package com.example.emergencyresponder.modules.dashboard.data.repositoryImpl

import com.example.emergencyresponder.modules.auth.data.model.EmergencyContact
import com.example.emergencyresponder.modules.dashboard.data.datasource.EmergencyContactRemoteDataSource
import com.example.emergencyresponder.modules.dashboard.domain.repository.EmergencyContactRepository

class EmergencyContactRepositoryImpl(
    private val remote: EmergencyContactRemoteDataSource
) : EmergencyContactRepository {

    override fun observeContacts(uid: String, onUpdate: (List<EmergencyContact>) -> Unit, onError: (String) -> Unit) {
        remote.observeContacts(uid, onUpdate, onError)
    }

    override fun addContact(uid: String, contact: EmergencyContact, onResult: (Boolean) -> Unit) {
        remote.addContact(uid, contact, onResult)
    }
}
