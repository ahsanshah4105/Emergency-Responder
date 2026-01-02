package com.example.emergencyresponder.modules.dashboard.ui.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.emergencyresponder.R
import com.example.emergencyresponder.core.constants.Sensitivity
import com.example.emergencyresponder.modules.dashboard.domain.useCase.CrashDetectionUseCase
import com.example.emergencyresponder.modules.dashboard.domain.useCase.SensorState
import kotlin.math.acos
import kotlin.math.sqrt

class CrashDetectionService : Service(), SensorEventListener {


    private lateinit var sensorManager: SensorManager
    private var sensitivity = Sensitivity.LOW
    private val crashUseCase = CrashDetectionUseCase(sensitivity)

    // --- Sensor storage ---
    private var lastAccel: FloatArray? = null
    private var lastGyro: FloatArray? = null
    private var lastGravity: FloatArray? = null
    private var previousGravity: FloatArray? = null
    private var isProximityNear = true



    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        register(Sensor.TYPE_LINEAR_ACCELERATION)
        register(Sensor.TYPE_GYROSCOPE)
        register(Sensor.TYPE_GRAVITY)
        register(Sensor.TYPE_PROXIMITY)

        startForegroundService()
    }

    private fun register(type: Int) {
        sensorManager.getDefaultSensor(type)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }


    override fun onSensorChanged(event: SensorEvent) {
        when(event.sensor.type){
            Sensor.TYPE_LINEAR_ACCELERATION -> lastAccel = event.values.clone()
            Sensor.TYPE_GYROSCOPE -> lastGyro = event.values.clone()
            Sensor.TYPE_GRAVITY -> lastGravity = event.values.clone()
            Sensor.TYPE_PROXIMITY -> isProximityNear = event.values[0] < event.sensor.maximumRange
        }

        if(lastAccel != null && lastGyro != null && lastGravity != null){
            val state = SensorState(
                accel = CrashDetectionUseCase.magnitude(lastAccel!!),
                gyro = CrashDetectionUseCase.magnitude(lastGyro!!),
                gravityAngle = CrashDetectionUseCase.gravityAngle(
                    previousGravity ?: lastGravity!!,
                    lastGravity!!
                ),
                proximityNear = isProximityNear
            )

            if(crashUseCase.evaluateCrash(state)) triggerCrashAlert()
            if(crashUseCase.evaluateSnatch(state)) triggerSnatchAlert()
        }

        previousGravity = lastGravity?.clone()
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit



    // --- Alert triggers ---
    private fun triggerCrashAlert() {
        Log.d("CrashDetection", "🚨 Crash detected")
        Toast.makeText(this, "🚨 Crash detected", Toast.LENGTH_SHORT).show()
    }

    private fun triggerSnatchAlert() {
        Log.d("SnatchingDetection", "📱 Snatching detected")
        Toast.makeText(this, "📱 Snatching detected", Toast.LENGTH_SHORT).show()
    }

    // --- Utility ---
    private fun magnitude(v: FloatArray): Double =
        sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2].toDouble())

    private fun gravityAngle(a: FloatArray, b: FloatArray): Double {
        val dot = a[0] * b[0] + a[1] * b[1] + a[2] * b[2]
        val mag = magnitude(a) * magnitude(b)
        val cos = (dot / mag).coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(cos))
    }

    // --- Foreground service ---
    private fun startForegroundService() {
        val channelId = "safety_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Safety Monitor",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Emergency Monitor Active")
            .setContentText("Monitoring motion sensors")
            .setSmallIcon(R.drawable.app_logo)
            .build()

        startForeground(101, notification)
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        stopForeground(true)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
}
