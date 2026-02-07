package com.example.emergencyresponder.modules.dashboard.domain.viewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.emergencyresponder.modules.auth.domain.useCase.UpdateProfileUseCase
import com.example.emergencyresponder.modules.dashboard.domain.viewmodel.ProfileViewModel

class ProfileViewModelFactory(
    private val useCase: UpdateProfileUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(useCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}