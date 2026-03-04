package com.example.emergencyresponder.core.base

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.emergencyresponder.core.utils.NotificationHelper

abstract class BaseForegroundService : Service() {

    abstract val notificationId: Int
    abstract val notificationTitle: String
    abstract val notificationContent: String

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationHelper.getBaseNotification(
            this, notificationTitle, notificationContent
        )
        startForeground(notificationId, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}