package com.example.emergencyresponder.modules.dashboard.domain.viewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.emergencyresponder.modules.auth.domain.useCase.ChangeEmailUseCase
import com.example.emergencyresponder.modules.auth.domain.useCase.UpdateProfileUseCase
import com.example.emergencyresponder.modules.dashboard.domain.viewmodel.ProfileViewModel

class ProfileViewModelFactory(
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val changeEmailUseCase: ChangeEmailUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(
            updateProfileUseCase,
            changeEmailUseCase
        ) as T
    }
}
