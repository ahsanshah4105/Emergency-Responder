package com.example.emergencyresponder.core.base

import android.app.Application
import com.example.emergencyresponder.core.di.AppContainer
import com.example.emergencyresponder.core.manager.SPreferenceManager

class EmergencyResponderApp : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        SPreferenceManager.init(this)
        appContainer = AppContainer(this)
    }
}