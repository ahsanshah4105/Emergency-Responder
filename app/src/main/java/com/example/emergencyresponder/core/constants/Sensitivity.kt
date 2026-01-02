package com.example.emergencyresponder.core.constants

enum class Sensitivity(
    val crashAccel: Double,
    val snatchAccel: Double,
    val gyro: Double,
    val minScore: Int
) {
    LOW(
        crashAccel = 25.0,
        snatchAccel = 15.0,
        gyro = 4.5,
        minScore = 1000
    ),
    MEDIUM(
        crashAccel = 22.0,
        snatchAccel = 12.0,
        gyro = 4.0,
        minScore = 850
    ),
    HIGH(
        crashAccel = 20.0,
        snatchAccel = 10.0,
        gyro = 3.5,
        minScore = 700
    )
}
