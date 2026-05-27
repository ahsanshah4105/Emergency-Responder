package com.example.emergencyresponder.core.base

import android.app.Application
import com.example.emergencyresponder.core.domain.coroutines.DispatcherProvider
import com.example.emergencyresponder.core.domain.repository.IBasePreference
import com.example.emergencyresponder.core.utils.SOSUtils
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class EmergencyResponderApp : Application() {

    @Inject lateinit var prefProvider: IBasePreference
    @Inject lateinit var dispatchers: DispatcherProvider

    override fun onCreate() {
        super.onCreate()
        // Initialize SOSUtils with DI-provided dependencies (keeps callers simple).
        SOSUtils.prefProvider = prefProvider
        SOSUtils.dispatchers = dispatchers
    }
}