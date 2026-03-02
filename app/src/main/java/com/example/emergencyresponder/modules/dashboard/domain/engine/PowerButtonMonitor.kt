package com.example.emergencyresponder.modules.dashboard.domain.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// modules/dashboard/domain/engine/PowerButtonMonitor.kt
class PowerButtonMonitor(private val context: Context) {
    private var powerPressCount = 0
    private var lastPressTime = 0L

    fun observeTriplePress(): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val now = System.currentTimeMillis()
                if (now - lastPressTime > 1000) powerPressCount = 0

                powerPressCount++
                lastPressTime = now

                if (powerPressCount == 3) {
                    powerPressCount = 0
                    trySend(Unit)
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        context.registerReceiver(receiver, filter)

        awaitClose { context.unregisterReceiver(receiver) }
    }
}