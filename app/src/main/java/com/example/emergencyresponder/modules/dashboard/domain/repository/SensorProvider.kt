package com.example.emergencyresponder.modules.dashboard.domain.repository

import com.example.emergencyresponder.modules.dashboard.data.model.SensorState
import kotlinx.coroutines.flow.Flow

interface SensorProvider {
    fun getSensorUpdates() : Flow<SensorState>
}