package com.example.emergencyresponder.modules.dashboard.data.model

data class SensorState(
    val accel: Double,
    val gyro: Double,
    val gravityAngle: Double,
    val proximityNear: Boolean
)
