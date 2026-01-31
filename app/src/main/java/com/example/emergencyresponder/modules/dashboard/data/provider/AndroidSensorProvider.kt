package com.example.emergencyresponder.modules.dashboard.data.provider

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.emergencyresponder.modules.dashboard.data.model.SensorState
import com.example.emergencyresponder.modules.dashboard.domain.repository.SensorProvider
import kotlin.math.sqrt

class AndroidSensorProvider(
    context: Context
) : SensorProvider, SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var callback: ((SensorState) -> Unit)? = null

    private var lastAccel: FloatArray? = null
    private var lastGyro: FloatArray? = null
    private var lastGravity: FloatArray? = null
    private var proximityNear = true

    override fun start(onUpdate: (SensorState) -> Unit) {
        callback = onUpdate

        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
            ?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }

        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            ?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }

        sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }

        sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
            ?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> lastAccel = event.values.clone()
            Sensor.TYPE_GYROSCOPE -> lastGyro = event.values.clone()
            Sensor.TYPE_GRAVITY -> lastGravity = event.values.clone()
            Sensor.TYPE_PROXIMITY ->
                proximityNear = event.values[0] < event.sensor.maximumRange
        }

        if (lastAccel == null || lastGyro == null || lastGravity == null) return

        val accelMag = magnitude(lastAccel!!)
        val gyroMag = magnitude(lastGyro!!)
        val gravityAngle = gravityAngle(lastGravity!!, lastAccel!!)

        val state = SensorState(
            accel = accelMag,
            gyro = gyroMag,
            gravityAngle = gravityAngle,
            proximityNear = proximityNear,
            mlConfidence = 0f // ML fills this later
        )

        callback?.invoke(state)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun magnitude(v: FloatArray): Double =
        sqrt((v[0] * v[0] + v[1] * v[1] + v[2] * v[2]).toDouble())

    private fun gravityAngle(a: FloatArray, b: FloatArray): Double {
        val dot = a[0] * b[0] + a[1] * b[1] + a[2] * b[2]
        val mag = magnitude(a) * magnitude(b)
        return Math.toDegrees(kotlin.math.acos((dot / mag).coerceIn(-1.0, 1.0)))
    }
}
