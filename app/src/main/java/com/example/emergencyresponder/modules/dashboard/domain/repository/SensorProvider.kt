package com.example.emergencyresponder.modules.dashboard.domain.repository

import com.example.emergencyresponder.modules.dashboard.data.model.SensorState

interface SensorProvider {
    fun start(onUpdate: (SensorState) -> Unit)
    fun stop()
}