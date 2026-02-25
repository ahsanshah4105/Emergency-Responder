package com.example.emergencyresponder.core.base

import android.app.Application
import com.example.emergencyresponder.core.manager.SPreferenceManager

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SPreferenceManager.init(this)
    }
}