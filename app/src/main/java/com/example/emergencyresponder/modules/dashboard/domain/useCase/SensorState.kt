package com.example.emergencyresponder.modules.dashboard.domain.useCase



data class SensorState(
    val accel: Double,
    val gyro: Double,
    val gravityAngle: Double,
    val proximityNear: Boolean,
)

