package com.example.emergencyresponder.modules.dashboard.domain.useCase


import Sensitivity
import com.example.emergencyresponder.modules.dashboard.data.model.DetectionResult
import com.example.emergencyresponder.modules.dashboard.data.model.SensorState
import kotlin.math.acos
import kotlin.math.sqrt


class CrashDetectionUseCase(
    private val sensitivity: Sensitivity
) {
    private var lastCrashTime = 0L

    private val cooldownMs = 3000L

    fun process(state: SensorState): DetectionResult {
        val now = System.currentTimeMillis()

        val threshold = when (sensitivity) {
            Sensitivity.LOW -> 0.90f
            Sensitivity.MEDIUM -> 0.75f
            Sensitivity.HIGH -> 0.60f
        }

        val modelDecision = state.mlConfidence >= threshold
        val ruleDecision = isCrash(state)

        if (modelDecision && ruleDecision && now - lastCrashTime > cooldownMs) {
            lastCrashTime = now
            return DetectionResult.Crash
        }

        return DetectionResult.None
    }


    private fun isCrash(state: SensorState): Boolean {
        val scale = when(sensitivity) {
            Sensitivity.LOW -> 1.5
            Sensitivity.MEDIUM -> 1.0
            Sensitivity.HIGH -> 0.5
        }

        val accelThreshold = sensitivity.crashAccel * scale
        val gyroThreshold = sensitivity.gyro * scale
        val gravityThreshold = 30.0

        val accelTriggered = state.accel >= accelThreshold
        val gyroTriggered = state.gyro >= gyroThreshold
        val gravityChanged = state.gravityAngle >= gravityThreshold

        return if (state.proximityNear) {
            accelTriggered && (gyroTriggered || gravityChanged)
        } else {
            accelTriggered && gyroTriggered
        }
    }

    private fun isSnatch(state: SensorState): Boolean {
        val scale = when(sensitivity) {
            Sensitivity.LOW -> 1.5
            Sensitivity.MEDIUM -> 1.0
            Sensitivity.HIGH -> 0.5
        }

        val accelThreshold = sensitivity.snatchAccel * scale
        val gyroThreshold = sensitivity.gyro * scale

        val isSuddenAway = !state.proximityNear
        val highAccelSpike = state.accel >= accelThreshold
        val enoughRotation = state.gyro >= gyroThreshold

        return isSuddenAway && highAccelSpike && enoughRotation
    }

    companion object {
        fun magnitude(v: FloatArray): Double =
            sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2].toDouble())

        fun gravityAngle(a: FloatArray, b: FloatArray): Double {
            val dot = a[0]*b[0] + a[1]*b[1] + a[2]*b[2]
            val mag = magnitude(a) * magnitude(b)
            val cos = (dot / mag).coerceIn(-1.0, 1.0)
            return Math.toDegrees(acos(cos))
        }
    }
}
