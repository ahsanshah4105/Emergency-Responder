package com.example.emergencyresponder.modules.dashboard.data.provider

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.emergencyresponder.modules.dashboard.data.model.SensorState
import com.example.emergencyresponder.modules.dashboard.domain.repository.SensorProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.sqrt

class AndroidSensorProvider(private val context: Context) : SensorProvider {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    override fun getSensorUpdates(): Flow<SensorState> = callbackFlow {
        val listener = object : SensorEventListener {
            private var lastAccel: FloatArray? = null
            private var lastGyro: FloatArray? = null
            private var lastGravity: FloatArray? = null
            private var proximityNear = true

            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_LINEAR_ACCELERATION -> lastAccel = event.values.clone()
                    Sensor.TYPE_GYROSCOPE -> lastGyro = event.values.clone()
                    Sensor.TYPE_GRAVITY -> lastGravity = event.values.clone()
                    Sensor.TYPE_PROXIMITY -> proximityNear = event.values[0] < event.sensor.maximumRange
                }

                if (lastAccel != null && lastGyro != null && lastGravity != null) {
                    val state = SensorState(
                        accel = magnitude(lastAccel!!),
                        gyro = magnitude(lastGyro!!),
                        gravityAngle = calculateGravityAngle(lastGravity!!, lastAccel!!),
                        proximityNear = proximityNear,
                        mlConfidence = 0f
                    )
                    trySend(state) // Flow mein data bhej raha hai
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        // Registering Sensors
        val sensors = listOf(Sensor.TYPE_LINEAR_ACCELERATION, Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_GRAVITY, Sensor.TYPE_PROXIMITY)
        sensors.forEach { type ->
            sensorManager.getDefaultSensor(type)?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }

        awaitClose {
            sensorManager.unregisterListener(listener) // Auto cleanup! ✅
        }
    }
    private fun magnitude(v: FloatArray): Double =
        sqrt((v[0] * v[0] + v[1] * v[1] + v[2] * v[2]).toDouble())

    private fun calculateGravityAngle(a: FloatArray, b: FloatArray): Double {
        val dot = a[0] * b[0] + a[1] * b[1] + a[2] * b[2]
        val mag = magnitude(a) * magnitude(b)
        return Math.toDegrees(kotlin.math.acos((dot / mag).coerceIn(-1.0, 1.0)))
    }
}
