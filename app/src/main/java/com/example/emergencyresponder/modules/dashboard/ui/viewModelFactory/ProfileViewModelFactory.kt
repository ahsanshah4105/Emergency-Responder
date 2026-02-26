package com.example.emergencyresponder.modules.dashboard.ui.viewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.emergencyresponder.core.domain.repository.IBasePreference
import com.example.emergencyresponder.modules.dashboard.domain.repository.IProfileRepository
import com.example.emergencyresponder.modules.dashboard.domain.usecase.ChangeEmailUseCase
import com.example.emergencyresponder.modules.dashboard.domain.usecase.UpdateProfileUseCase
import com.example.emergencyresponder.modules.dashboard.ui.viewmodel.ProfileViewModel

class ProfileViewModelFactory(
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val changeEmailUseCase: ChangeEmailUseCase,
    private val repository: IProfileRepository,
    private val prefProvider: IBasePreference
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(
            updateProfileUseCase = updateProfileUseCase,
            changeEmailUseCase = changeEmailUseCase,
            repository = repository,
            prefProvider = prefProvider
        ) as T
    }
}
