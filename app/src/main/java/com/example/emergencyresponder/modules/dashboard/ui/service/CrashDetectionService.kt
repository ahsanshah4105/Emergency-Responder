package com.example.emergencyresponder.modules.dashboard.ui.service

import Sensitivity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
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
import com.example.emergencyresponder.core.utils.TFLiteModelHelper
import com.example.emergencyresponder.modules.dashboard.data.model.DetectionResult
import com.example.emergencyresponder.modules.dashboard.data.model.SensorState
import com.example.emergencyresponder.modules.dashboard.domain.useCase.CrashDetectionUseCase
import com.example.emergencyresponder.modules.dashboard.domain.useCase.CrashDetectionUseCase.Companion.magnitude
import com.example.emergencyresponder.modules.timestamp.ui.TimeStampActivity
import kotlin.math.pow

class CrashDetectionService : Service(), SensorEventListener {
    private val accelBuffer = mutableListOf<Float>()
    private val gyroBuffer = mutableListOf<Float>()
    private val linBuffer = mutableListOf<Float>()

    private val BUFFER_SIZE = 50

    private lateinit var modelHelper: TFLiteModelHelper
    private lateinit var sensorManager: SensorManager
    private var sensitivity = Sensitivity.HIGH
    private lateinit var crashUseCase: CrashDetectionUseCase

    // --- Sensor storage ---
    private var lastAccel: FloatArray? = null
    private var lastGyro: FloatArray? = null
    private var lastGravity: FloatArray? = null
    private var isProximityNear = true

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        modelHelper = TFLiteModelHelper(this)
        crashUseCase = CrashDetectionUseCase(sensitivity)

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
        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> lastAccel = event.values.clone()
            Sensor.TYPE_GYROSCOPE -> lastGyro = event.values.clone()
            Sensor.TYPE_GRAVITY -> lastGravity = event.values.clone()
            Sensor.TYPE_PROXIMITY ->
                isProximityNear = event.values[0] < event.sensor.maximumRange
        }

        if (lastAccel == null || lastGyro == null) return

        val accelMag = magnitude(lastAccel!!).toFloat()
        val gyroMag = magnitude(lastGyro!!).toFloat()
        val linMag = accelMag

        accelBuffer.add(accelMag)
        gyroBuffer.add(gyroMag)
        linBuffer.add(linMag)

        if (accelBuffer.size >= BUFFER_SIZE) {

            val features = floatArrayOf(
                accelBuffer.maxOrNull()!!,
                gyroBuffer.maxOrNull()!!,
                kurtosis(accelBuffer),
                kurtosis(gyroBuffer),
                linBuffer.maxOrNull()!!,
                skewness(accelBuffer),
                skewness(gyroBuffer),
                gyroBuffer.takeLast(5).maxOrNull()!!,
                linBuffer.takeLast(5).maxOrNull()!!
            )

            // **ML returns confidence**
            val confidence = modelHelper.predict(features)

            // **Create state**
            val state = SensorState(
                accel = accelBuffer.maxOrNull()!!.toDouble(),
                gyro = gyroBuffer.maxOrNull()!!.toDouble(),
                gravityAngle = CrashDetectionUseCase.gravityAngle(
                    lastGravity!!,
                    lastAccel!!
                ),
                proximityNear = isProximityNear,
                mlConfidence = confidence.toFloat()
            )

            // **UseCase decides**
            when (crashUseCase.process(state)) {
                DetectionResult.Crash -> triggerCrashAlert()
                DetectionResult.Snatch -> triggerSnatchAlert()
                DetectionResult.None -> { /* do nothing */ }
            }

            accelBuffer.clear()
            gyroBuffer.clear()
            linBuffer.clear()
        }
    }
    private fun mean(data: List<Float>): Float =
        data.sum() / data.size

    private fun skewness(data: List<Float>): Float {
        val m = mean(data)
        val sd = kotlin.math.sqrt(data.map { (it - m) * (it - m) }.sum() / data.size)
        if (sd == 0f) return 0f
        return (data.map { (it - m).pow(3) }.sum() / data.size) / sd.pow(3)
    }

    private fun kurtosis(data: List<Float>): Float {
        val m = mean(data)
        val sd = kotlin.math.sqrt(data.map { (it - m) * (it - m) }.sum() / data.size)
        if (sd == 0f) return 0f
        return (data.map { (it - m).pow(4) }.sum() / data.size) / sd.pow(4)
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // sensitivity update
        val level = intent?.getStringExtra("sensitivity") ?: "$sensitivity"

        sensitivity = when(level.uppercase()) {
            "MEDIUM" -> Sensitivity.MEDIUM
            "HIGH" -> Sensitivity.HIGH
            else -> Sensitivity.LOW
        }

        crashUseCase = CrashDetectionUseCase(sensitivity)  // update usecase
        return START_STICKY
    }

    private fun triggerCrashAlert() {
        val hasPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED
            } else true

        if (!hasPermission) {
            Log.d("CrashDetectionService", "Notification permission missing — crash detected silently")
            return
        }

        val intent = Intent(this, TimeStampActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "safety_channel")
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle("🚨 Crash Detected")
            .setContentText("Tap to open emergency details")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(102, notification)
    }

    private fun triggerSnatchAlert() {
        Log.d("SnatchingDetection", "📱 Snatching detected")
        Toast.makeText(this, "📱 Snatching detected", Toast.LENGTH_SHORT).show()
    }

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
}
