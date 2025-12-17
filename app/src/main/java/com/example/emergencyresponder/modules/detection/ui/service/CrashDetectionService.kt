package com.example.emergencyresponder.modules.detection.ui.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.emergencyresponder.R

class CrashDetectionService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accel: Sensor? = null
    private var gyro: Sensor? = null

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)


        accel?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        gyro?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }

        showNotificationAndStartForeground()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                Log.d("SensorTracker", "Accel Force: ${String.format("%.2f", x)}, ${String.format("%.2f", y)}, ${String.format("%.2f", z)}")
            }
            Sensor.TYPE_GYROSCOPE -> {
                val vX = event.values[0] // Angular velocity
                val vY = event.values[1]
                val vZ = event.values[2]
                Log.d("SensorTracker", "Rotation Velocity: ${String.format("%.2f", vX)}")
            }
        }
    }

    private fun showNotificationAndStartForeground() {
        val channelId = "emergency_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "Safety Service", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Emergency Monitor Active")
            .setContentText("Monitoring sensors for accident detection...")
            .setSmallIcon(R.drawable.app_logo)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(101, notification)
    }
    override fun onDestroy() {
        sensorManager.unregisterListener(this)

        stopForeground(true)
        super.onDestroy()
        Log.d("CrashService", "Service Stopped and Sensors Off")
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}