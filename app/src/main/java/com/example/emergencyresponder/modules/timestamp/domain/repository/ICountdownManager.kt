package com.example.emergencyresponder.modules.timestamp.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface ICountdownManager {
    val remainingSeconds: StateFlow<Long>
    fun startCountdown(onSosAction: (() -> Unit)?)
    fun cancel()
    val totalTimeSec: Int
}