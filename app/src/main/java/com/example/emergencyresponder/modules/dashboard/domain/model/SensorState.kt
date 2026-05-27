package com.example.emergencyresponder.modules.dashboard.domain.model

/**
 * Domain model for current sensor state used in crash/snatch detection.
 */
data class SensorState(
    val accel: Double,
    val gyro: Double,
    val gravityAngle: Double,
    val proximityNear: Boolean,
    val mlConfidence: Float
)
