package com.example.emergencyresponder.modules.dashboard.domain.engine

import com.example.emergencyresponder.modules.dashboard.domain.model.SensorState
import kotlin.math.pow
import kotlin.math.sqrt

class FeatureExtractor(
    private val bufferSize: Int = 50
) {

    private val accelBuffer = mutableListOf<Double>()
    private val gyroBuffer = mutableListOf<Double>()

    fun extract(state: SensorState): FloatArray? {
        accelBuffer.add(state.accel.toDouble())
        gyroBuffer.add(state.gyro.toDouble())

        if (accelBuffer.size < bufferSize) return null

        val features = floatArrayOf(
            accelBuffer.maxOrNull()!!.toFloat(),
            gyroBuffer.maxOrNull()!!.toFloat(),
            kurtosis(accelBuffer).toFloat(),
            kurtosis(gyroBuffer).toFloat(),
            accelBuffer.maxOrNull()!!.toFloat(), // lin max
            skewness(accelBuffer).toFloat(),
            skewness(gyroBuffer).toFloat(),
            gyroBuffer.takeLast(5).maxOrNull()!!.toFloat(),
            accelBuffer.takeLast(5).maxOrNull()!!.toFloat()
        )

        accelBuffer.clear()
        gyroBuffer.clear()

        return features
    }

    private fun mean(data: List<Double>): Double =
        data.sum() / data.size

    private fun skewness(data: List<Double>): Double {
        val m = mean(data)
        val sd = sqrt(data.sumOf { (it - m).pow(2) } / data.size)
        if (sd == 0.0) return 0.0
        return (data.sumOf { (it - m).pow(3) } / data.size) / sd.pow(3)
    }

    private fun kurtosis(data: List<Double>): Double {
        val m = mean(data)
        val sd = sqrt(data.sumOf { (it - m).pow(2) } / data.size)
        if (sd == 0.0) return 0.0
        return (data.sumOf { (it - m).pow(4) } / data.size) / sd.pow(4)
    }
}
