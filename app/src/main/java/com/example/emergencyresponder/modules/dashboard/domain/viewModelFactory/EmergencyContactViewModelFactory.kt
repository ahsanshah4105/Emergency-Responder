package com.example.emergencyresponder.modules.dashboard.domain.viewModelFactory

import AddEmergencyContactUseCase
import ObserveEmergencyContactsUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.example.emergencyresponder.modules.dashboard.domain.viewmodel.EmergencyContactViewModel

class EmergencyContactViewModelFactory(
    private val observeUseCase: ObserveEmergencyContactsUseCase,
    private val addUseCase: AddEmergencyContactUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmergencyContactViewModel::class.java)) {
            return EmergencyContactViewModel(observeUseCase, addUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
