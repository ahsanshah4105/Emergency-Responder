package com.example.emergencyresponder.modules.dashboard.domain.engine

import android.os.SystemClock

class PowerPressDetector(
    private val requiredPresses: Int = 5,
    private val maxDelay: Long = 1000L,
    private val onTrigger: () -> Unit
) {

    private var pressCount = 0
    private var lastPressTime = 0L

    fun registerPress() {
        val currentTime = SystemClock.elapsedRealtime()

        if (currentTime - lastPressTime > maxDelay) {
            pressCount = 0
        }

        pressCount++
        lastPressTime = currentTime

        if (pressCount == requiredPresses) {
            pressCount = 0
            onTrigger()
        }
    }
}
