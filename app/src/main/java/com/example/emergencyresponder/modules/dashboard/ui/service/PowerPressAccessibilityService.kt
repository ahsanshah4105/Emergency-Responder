package com.example.emergencyresponder.modules.dashboard.ui.service


import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat

class PowerPressAccessibilityService : AccessibilityService() {

    private var pressCount = 0
    private var lastPressTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        // Detect screen ON/OFF events (Power button triggers these)
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            val currentTime = SystemClock.elapsedRealtime()

            // Reset counter if delay > 1 second
            if (currentTime - lastPressTime > 1000) {
                pressCount = 0
            }

            pressCount++
            lastPressTime = currentTime

            // Triple press detected
            if (pressCount == 3) {
                pressCount = 0
                triggerEmergency()
            }
        }
    }

    private fun triggerEmergency() {

        val intent = Intent(this, CrashDetectionService::class.java).apply {
            putExtra("TRIPLE_POWER_PRESS", true)
        }

        ContextCompat.startForegroundService(this, intent)
    }

    override fun onInterrupt() {
        // Required override
    }
}
