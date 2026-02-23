package com.example.emergencyresponder.modules.dashboard.ui.viewmodel

import AddEmergencyContactUseCase
import ObserveEmergencyContactsUseCase
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.emergencyresponder.modules.auth.data.model.EmergencyContact
import com.example.emergencyresponder.modules.dashboard.domain.repository.EmergencyContactRepository


class EmergencyContactViewModel(
    private val observeUseCase: ObserveEmergencyContactsUseCase,
    private val addUseCase: AddEmergencyContactUseCase,
    private val repository: EmergencyContactRepository
) : ViewModel() {

    private val _contacts = MutableLiveData<List<EmergencyContact>>()
    val contacts: LiveData<List<EmergencyContact>> = _contacts

    fun observeContacts(uid: String) {
        observeUseCase(uid,
            onUpdate = { _contacts.value = it },
            onError = { }
        )
    }


    // Logic for Deleting
    fun deleteContact(uid: String, contact: EmergencyContact) {
        repository.deleteContact(uid, contact) { success ->
            // The observeContacts listener will automatically update the UI list
        }
    }

    fun addContact(uid: String, contact: EmergencyContact) {
        addUseCase(uid, contact) {}
    }
}
