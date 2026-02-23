package com.example.emergencyresponder.modules.dashboard.ui.viewModelFactory

import AddEmergencyContactUseCase
import ObserveEmergencyContactsUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.emergencyresponder.modules.dashboard.domain.repository.EmergencyContactRepository

import com.example.emergencyresponder.modules.dashboard.ui.viewmodel.EmergencyContactViewModel

class EmergencyContactViewModelFactory(
    private val observeUseCase: ObserveEmergencyContactsUseCase,
    private val addUseCase: AddEmergencyContactUseCase,
    private val repository: EmergencyContactRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmergencyContactViewModel::class.java)) {
            return EmergencyContactViewModel(observeUseCase, addUseCase, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
