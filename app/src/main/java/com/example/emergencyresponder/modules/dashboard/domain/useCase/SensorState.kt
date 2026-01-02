package com.example.emergencyresponder.modules.dashboard.domain.useCase

import kotlin.math.sqrt


import com.example.emergencyresponder.core.constants.Sensitivity
import kotlin.math.acos

data class SensorState(
    val accel: Double,
    val gyro: Double,
    val gravityAngle: Double,
    val proximityNear: Boolean,
)

class CrashDetectionUseCase(private val sensitivity: Sensitivity) {

    fun evaluateCrash(state: SensorState): Boolean {
        val accelTriggered = state.accel > sensitivity.crashAccel
        val gyroTriggered = state.gyro > sensitivity.gyro
        val gravityTriggered = state.gravityAngle > 35
        return (accelTriggered && gyroTriggered) || (accelTriggered && gravityTriggered)
    }

    fun evaluateSnatch(state: SensorState): Boolean {
        val accelTriggered = state.accel > sensitivity.snatchAccel
        val gravityTriggered = state.gravityAngle > 35
        val proximityTriggered = !state.proximityNear
        return (proximityTriggered && accelTriggered) || (proximityTriggered && gravityTriggered)
    }

    companion object {
        fun magnitude(v: FloatArray): Double =
            sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2].toDouble())

        fun gravityAngle(a: FloatArray, b: FloatArray): Double {
            val dot = a[0] * b[0] + a[1] * b[1] + a[2] * b[2]
            val mag = magnitude(a) * magnitude(b)
            val cos = (dot / mag).coerceIn(-1.0, 1.0)
            return Math.toDegrees(acos(cos))
        }
    }
}
