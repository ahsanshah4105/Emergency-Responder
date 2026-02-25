package com.example.emergencyresponder.modules.onboarding.ui.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.emergencyresponder.modules.onboarding.domain.repository.IOnboardingRepository
import com.example.emergencyresponder.modules.onboarding.ui.viewmodel.OnboardingViewModel

class OnboardingViewModelFactory(
    private val onboardingRepository: IOnboardingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OnboardingViewModel(onboardingRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
