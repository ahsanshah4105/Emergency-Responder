package com.example.emergencyresponder.core.di

import android.content.Context
import com.example.emergencyresponder.core.data.repository.PreferenceProviderImpl
import com.example.emergencyresponder.core.domain.repository.IBasePreference
import com.example.emergencyresponder.modules.onboarding.data.repository.IOnboardingRepositoryImpl
import com.example.emergencyresponder.modules.onboarding.domain.repository.IOnboardingRepository
import com.example.emergencyresponder.modules.splash.data.repository.ISplashRepositoryImpl
import com.example.emergencyresponder.modules.timestamp.data.repository.ICrashRepositoryImpl
import com.example.emergencyresponder.modules.timestamp.domain.repository.ICrashRepository
import com.example.emergencyresponder.modules.splash.domain.repository.ISplashRepository

class AppContainer(private val context: Context) {

    private val prefProvider: IBasePreference by lazy {
        PreferenceProviderImpl(context.applicationContext)
    }

    val crashRepository: ICrashRepository by lazy {
        ICrashRepositoryImpl(prefProvider)
    }

    val splashRepository: ISplashRepository by lazy {
        ISplashRepositoryImpl(prefProvider)
    }

    val onboardingRepository: IOnboardingRepository by lazy {
        IOnboardingRepositoryImpl(prefProvider)
    }
}