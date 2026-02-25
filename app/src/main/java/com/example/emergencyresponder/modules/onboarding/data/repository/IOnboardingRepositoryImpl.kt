package com.example.emergencyresponder.modules.onboarding.data.repository

import com.example.emergencyresponder.core.constants.PrefKeys
import com.example.emergencyresponder.core.domain.repository.IBasePreference
import com.example.emergencyresponder.modules.onboarding.domain.repository.IOnboardingRepository

class IOnboardingRepositoryImpl(private val prefs: IBasePreference) : IOnboardingRepository {
    override fun completeOnboarding() {
        prefs.saveBoolean(PrefKeys.ONBOARDING_COMPLETED, true)
    }
}