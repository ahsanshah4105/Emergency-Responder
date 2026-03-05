package com.example.emergencyresponder.modules.dashboard.data.service


import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import com.example.emergencyresponder.modules.dashboard.domain.engine.PowerPressDetector
import android.view.accessibility.AccessibilityEvent
import com.example.emergencyresponder.modules.dashboard.domain.usecase.TriggerEmergencyUseCase
import com.example.emergencyresponder.modules.dashboard.data.notifierImpl.AndroidEmergencyTrigger

class PowerPressAccessibilityService : AccessibilityService() {

    private val detector by lazy {
        val trigger = AndroidEmergencyTrigger(applicationContext)
        val useCase = TriggerEmergencyUseCase(trigger)
        PowerPressDetector(requiredPresses = 3, maxDelay = 1500L) {
            useCase.execute()
        }
    }

    private val screenReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_ON || intent?.action == Intent.ACTION_SCREEN_OFF) {
                detector.registerPress()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = android.content.IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenReceiver)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {}
}