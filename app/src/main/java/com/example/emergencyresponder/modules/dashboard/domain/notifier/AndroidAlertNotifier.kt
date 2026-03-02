package com.example.emergencyresponder.modules.dashboard.domain.notifier

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.emergencyresponder.R
import com.example.emergencyresponder.modules.timestamp.ui.EmergencyAlertActivity

class AndroidAlertNotifier(private val context: Context) : AlertNotifier {

    // ID change kar di taake system fresh channel create kare HIGH importance k sath
    private val channelId = "emergency_alerts_v1"
    private val notificationId = 101

    init { createChannel() }

    private fun showEmergencyNotification(title: String, message: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, EmergencyAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("REMAINING_SECONDS", 30)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX) // Screen par pop-up hoga
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound + Vibrate + Lights
            .setFullScreenIntent(pendingIntent, true) // 👈 Sab se zaroori: Screen wake up
            .setOngoing(true) // Emergency hai, user swipe na kar sakay
            .setAutoCancel(true)
            .build()

        Log.d("AlertNotifier", "Attempting to post notification: $title")
        manager.notify(notificationId, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                channelId,
                "Emergency Notifications",
                NotificationManager.IMPORTANCE_HIGH // Screen par dikhane k liye zaroori hai
            ).apply {
                description = "Critical alerts for life safety"
                enableVibration(true)
                enableLights(true)
                setBypassDnd(true) // DND mode ko bypass kare
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }
    }

    override fun notifyCrash() = showEmergencyNotification("🚨 Crash Detected", "Impact detected! SOS starting.")
    override fun notifySnatch() = showEmergencyNotification("✋ Snatch Detected", "Device movement detected.")
    override fun notifyManualSOS() = showEmergencyNotification("🆘 SOS Activated", "Manual emergency triggered.")
    override fun cancel() {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(notificationId)
    }
}