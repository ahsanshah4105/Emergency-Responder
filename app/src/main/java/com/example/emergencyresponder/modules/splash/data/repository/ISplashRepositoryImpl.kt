package com.example.emergencyresponder.modules.splash.data.repository

import com.example.emergencyresponder.core.common.PrefKeys
import com.example.emergencyresponder.core.domain.repository.IBasePreference
import com.example.emergencyresponder.modules.splash.domain.repository.ISplashRepository

class ISplashRepositoryImpl(
    private val prefProvider: IBasePreference

) : ISplashRepository {
    override fun isUserLoggedIn(): Boolean = prefProvider.getBoolean(PrefKeys.USER_LOGGED_IN, false)

    override fun isOnboardingCompleted(): Boolean =
        prefProvider.getBoolean(PrefKeys.ONBOARDING_COMPLETED, false)
}