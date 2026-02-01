package com.example.emergencyresponder.modules.dashboard.ui.service


import android.accessibilityservice.AccessibilityService
import android.content.Intent
import com.example.emergencyresponder.modules.dashboard.domain.engine.PowerPressDetector
import android.view.accessibility.AccessibilityEvent
import com.example.emergencyresponder.modules.dashboard.domain.useCase.TriggerEmergencyUseCase
import com.example.emergencyresponder.modules.dashboard.data.notifierImpl.AndroidEmergencyTrigger
class PowerPressAccessibilityService : AccessibilityService() {

    private val detector by lazy {

        val trigger = AndroidEmergencyTrigger(applicationContext)
        val useCase = TriggerEmergencyUseCase(trigger)

        PowerPressDetector(
            requiredPresses = 5,
            maxDelay = 1000L
        ) {
            useCase.execute()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            detector.registerPress()
        }
    }

    override fun onInterrupt() {}
}
