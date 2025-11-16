package com.example.emergencyresponder.core.constants

import android.app.Application
import com.example.emergencyresponder.core.objects.SPreferenceManager

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SPreferenceManager.init(this)
    }
}