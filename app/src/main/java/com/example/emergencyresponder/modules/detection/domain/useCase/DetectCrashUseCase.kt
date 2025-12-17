package com.example.emergencyresponder.modules.detection.domain.useCase

class DetectCrashUseCase {

    private val CRASH_THRESHOLD = 25.0 // ~2.5Gs

    fun isCrashDetected(x: Float, y: Float, z: Float): Boolean {
        // Total Magnitude Formula: sqrt(x² + y² + z²)
        val totalAcceleration = Math.sqrt((x * x + y * y + z * z).toDouble())

        // Logic: If acceleration exceeds threshold, it's a potential crash
        return totalAcceleration > CRASH_THRESHOLD
    }
}