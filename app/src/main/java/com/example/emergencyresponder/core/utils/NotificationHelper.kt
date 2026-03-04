package com.example.emergencyresponder.core.utils

import android.app.*
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.emergencyresponder.R

object NotificationHelper {
    const val SERVICE_CHANNEL_ID = "safety_service_channel"
    const val ALARM_CHANNEL_ID = "emergency_alarm_channel"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                "Safety Monitoring Service",
                NotificationManager.IMPORTANCE_LOW
            )

            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Emergency Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
            manager.createNotificationChannel(alarmChannel)
        }
    }

    fun getBaseNotification(context: Context, title: String, content: String): Notification {
        return NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.app_logo)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}