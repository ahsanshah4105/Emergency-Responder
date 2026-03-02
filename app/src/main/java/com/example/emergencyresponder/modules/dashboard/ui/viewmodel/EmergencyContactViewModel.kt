package com.example.emergencyresponder.modules.dashboard.ui.viewmodel

import AddEmergencyContactUseCase
import ObserveEmergencyContactsUseCase
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencyresponder.core.base.Event
import com.example.emergencyresponder.modules.auth.data.model.EmergencyContact
import com.example.emergencyresponder.modules.dashboard.domain.usecase.DeleteEmergencyContactUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch


class EmergencyContactViewModel(
    private val uid: String,
    private val observeUseCase: ObserveEmergencyContactsUseCase,
    private val addUseCase: AddEmergencyContactUseCase,
    private val deleteUseCase: DeleteEmergencyContactUseCase,
) : ViewModel() {
    private val _contacts = MutableLiveData<List<EmergencyContact>>()
    val contacts: LiveData<List<EmergencyContact>> = _contacts

    private val _error = MutableLiveData<Event<EmergencyError>>()
    val error: LiveData<Event<EmergencyError>> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        observeContacts()
    }

    private fun observeContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            observeUseCase(uid)
                .catch { e ->
                    _error.value = Event(EmergencyError.NETWORK_ERROR)
                    _isLoading.value = false
                }
                .collect { list ->
                    _contacts.value = list
                    _isLoading.value = false
                }
        }
    }

    fun deleteContact(contact: EmergencyContact) {
        viewModelScope.launch {
            try {
                deleteUseCase(uid, contact)
            } catch (e: Exception) {
                _error.value = Event(EmergencyError.FAILED_TO_DELETE)
            }
        }
    }

    fun addContact(contact: EmergencyContact) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                addUseCase(uid, contact)
            } catch (e: Exception) {
                _error.value = Event(EmergencyError.FAILED_TO_ADD)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

enum class EmergencyError {
    FAILED_TO_DELETE,
    FAILED_TO_ADD,
    NETWORK_ERROR,
    UNKNOWN
}