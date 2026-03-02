package com.example.emergencyresponder.modules.dashboard.ui.viewModelFactory

import AddEmergencyContactUseCase
import ObserveEmergencyContactsUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.emergencyresponder.modules.dashboard.domain.usecase.DeleteEmergencyContactUseCase
import com.example.emergencyresponder.modules.dashboard.ui.viewmodel.EmergencyContactViewModel

class EmergencyContactViewModelFactory(
    private val uid: String,
    private val observeUseCase: ObserveEmergencyContactsUseCase,
    private val addUseCase: AddEmergencyContactUseCase,
    private val deleteUseCase: DeleteEmergencyContactUseCase,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmergencyContactViewModel::class.java)) {
            return EmergencyContactViewModel(
                uid = uid,
                observeUseCase = observeUseCase,
                addUseCase = addUseCase,
                deleteUseCase = deleteUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
