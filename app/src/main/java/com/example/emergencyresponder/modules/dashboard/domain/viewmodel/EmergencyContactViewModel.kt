package com.example.emergencyresponder.modules.dashboard.domain.viewmodel

import AddEmergencyContactUseCase
import ObserveEmergencyContactsUseCase
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.emergencyresponder.modules.auth.data.model.EmergencyContact


class EmergencyContactViewModel(
    private val observeUseCase: ObserveEmergencyContactsUseCase,
    private val addUseCase: AddEmergencyContactUseCase
) : ViewModel() {

    private val _contacts = MutableLiveData<List<EmergencyContact>>()
    val contacts: LiveData<List<EmergencyContact>> = _contacts

    fun observeContacts(uid: String) {
        observeUseCase(uid,
            onUpdate = { _contacts.value = it },
            onError = { }
        )
    }

    fun addContact(uid: String, contact: EmergencyContact) {
        addUseCase(uid, contact) {}
    }
}
