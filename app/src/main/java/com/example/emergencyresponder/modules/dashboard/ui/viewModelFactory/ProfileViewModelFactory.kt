package com.example.emergencyresponder.modules.dashboard.ui.viewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.emergencyresponder.modules.auth.domain.usecase.ChangeEmailUseCase
import com.example.emergencyresponder.modules.auth.domain.usecase.UpdateProfileUseCase
import com.example.emergencyresponder.modules.dashboard.ui.viewmodel.ProfileViewModel

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
