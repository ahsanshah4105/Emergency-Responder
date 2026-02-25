package com.example.emergencyresponder.core.di

import android.content.Context
import com.example.emergencyresponder.core.data.repository.PreferenceProviderImpl
import com.example.emergencyresponder.core.domain.repository.IBasePreference
import com.example.emergencyresponder.modules.timestamp.data.repository.ICrashRepositoryImpl
import com.example.emergencyresponder.modules.timestamp.domain.repository.ICrashRepository


class AppContainer(private val context: Context) {

    private val prefProvider: IBasePreference by lazy {
        PreferenceProviderImpl(context.applicationContext)
    }

    val crashRepository: ICrashRepository by lazy {
        ICrashRepositoryImpl(prefProvider)
    }
}