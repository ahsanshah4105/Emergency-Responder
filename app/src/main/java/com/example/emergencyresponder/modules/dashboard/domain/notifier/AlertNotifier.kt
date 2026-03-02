package com.example.emergencyresponder.modules.dashboard.domain.notifier

import android.content.Context

interface AlertNotifier {
    fun notifyCrash()
    fun notifySnatch()
    fun notifyManualSOS()
    fun cancel()
}
enum class AlertType { CRASH, SNATCH, MANUAL_SOS }