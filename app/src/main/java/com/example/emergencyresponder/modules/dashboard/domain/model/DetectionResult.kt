package com.example.emergencyresponder.modules.dashboard.domain.model

/**
 * Domain result of crash/snatch detection evaluation.
 */
sealed class DetectionResult {
    data object Crash : DetectionResult()
    data object Snatch : DetectionResult()
    data object None : DetectionResult()
}
