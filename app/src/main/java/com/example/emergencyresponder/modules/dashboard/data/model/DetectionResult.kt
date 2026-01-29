package com.example.emergencyresponder.modules.dashboard.data.model

sealed class  DetectionResult {
    object Crash : DetectionResult()
    object Snatch : DetectionResult()
    object None : DetectionResult()
}
