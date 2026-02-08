package com.example.emergencyresponder.modules.dashboard.domain.notifier

import android.content.Context
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.emergencyresponder.R
import com.example.emergencyresponder.modules.dashboard.ui.service.CrashDetectionService
import com.example.emergencyresponder.modules.timestamp.ui.TimeStampActivity

class AndroidAlertNotifier(
    private val context: Context
) : AlertNotifier {

    private val channelId = "safety_channel"
    private val notificationId = 101 // Unique ID for emergency alerts

    init {
        createChannelIfNeeded()
    }

    // 1. CRASH
    override fun notifyCrash() {
        showEmergencyNotification(
            title = "🚨 Crash Detected",
            message = "Impact detected. Tap to cancel SOS."
        )
    }

    // 2. SNATCH (Now triggers a real Notification, not just a Toast)
    override fun notifySnatch() {
        // Still show the toast for immediate visual feedback
        Toast.makeText(context, "📱 Snatch Detected", Toast.LENGTH_SHORT).show()

        showEmergencyNotification(
            title = "✋ Snatch Detected",
            message = "Sudden movement detected. Check device."
        )
    }

    // 3. MANUAL SOS (Power Button)
    override fun notifyManualSOS() {
        showEmergencyNotification(
            title = "🆘 SOS Activated",
            message = "Emergency shortcut triggered."
        )
    }

    // ✅ Shared Helper Function to build the notification
    private fun showEmergencyNotification(title: String, message: String) {

        // Prepare Intent to open the App/Timer screen
        val intent = Intent(context, TimeStampActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (context is CrashDetectionService) {
                putExtra("REMAINING_SECONDS", context.voiceManager.remainingSeconds.toInt())
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)   // Dynamic Title
            .setContentText(message)  // Dynamic Message
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true) // Optional: Pop up on screen
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Safety Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for Crash, Snatch, and SOS events"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
//
//class AndroidAlertNotifier(
//    private val context: Context
//) : AlertNotifier {
//
//    private val channelId = "safety_channel"
//
//    init {
//        createChannelIfNeeded()
//    }
//
//    override fun notifyCrash() {
//        val intent = Intent(context, TimeStampActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//            if (context is CrashDetectionService) {
//                putExtra("REMAINING_SECONDS", context.voiceManager.remainingSeconds.toInt())
//            }
//        }
//
//        val pendingIntent = PendingIntent.getActivity(
//            context,
//            0,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val notification = NotificationCompat.Builder(context, channelId)
//            .setSmallIcon(R.drawable.app_logo)
//            .setContentTitle("🚨 Crash Detected")
//            .setContentText("Tap to view emergency details")
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setCategory(NotificationCompat.CATEGORY_ALARM)
//            .setAutoCancel(true)
//            .setContentIntent(pendingIntent)
//            .build()
//
//        val manager =
//            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        manager.notify(101, notification)
//    }
//
//    override fun notifySnatch() {
//        Toast.makeText(context, "📱 Phone snatching detected", Toast.LENGTH_SHORT).show()
//    }
//
//    private fun createChannelIfNeeded() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                "Safety Alerts",
//                NotificationManager.IMPORTANCE_HIGH
//            )
//            val manager =
//                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            manager.createNotificationChannel(channel)
//        }
//    }
//}
