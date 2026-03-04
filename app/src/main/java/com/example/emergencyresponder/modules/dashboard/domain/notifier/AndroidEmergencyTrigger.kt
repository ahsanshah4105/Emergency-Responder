package com.example.emergencyresponder.modules.dashboard.data.notifierImpl

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.emergencyresponder.modules.dashboard.domain.notifier.EmergencyTrigger
import com.example.emergencyresponder.modules.dashboard.data.service.CrashDetectionService

class AndroidEmergencyTrigger(
    private val context: Context
) : EmergencyTrigger {

    override fun trigger() {

        val intent = Intent(context, CrashDetectionService::class.java).apply {
            putExtra("TRIPLE_POWER_PRESS", true)
        }

        ContextCompat.startForegroundService(context, intent)
    }
}
