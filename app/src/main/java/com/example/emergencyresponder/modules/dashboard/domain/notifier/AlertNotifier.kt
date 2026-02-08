package com.example.emergencyresponder.modules.dashboard.domain.notifier

import android.content.Context

interface AlertNotifier {
    fun notifyCrash()
    fun notifySnatch()
    fun notifyManualSOS()
}
