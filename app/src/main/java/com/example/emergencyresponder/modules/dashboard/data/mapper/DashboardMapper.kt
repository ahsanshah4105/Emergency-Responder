package com.example.emergencyresponder.modules.dashboard.data.mapper

import com.example.emergencyresponder.core.domain.model.EmergencyContact
import com.example.emergencyresponder.modules.auth.data.model.EmergencyContact as DataEmergencyContact
import com.example.emergencyresponder.modules.dashboard.data.model.DetectionResult as DataDetectionResult
import com.example.emergencyresponder.modules.dashboard.data.model.DashboardStatus as DataDashboardStatus
import com.example.emergencyresponder.modules.dashboard.data.model.SensorState as DataSensorState
import com.example.emergencyresponder.modules.dashboard.domain.model.DashboardStatus
import com.example.emergencyresponder.modules.dashboard.domain.model.DetectionResult
import com.example.emergencyresponder.modules.dashboard.domain.model.SensorState

/**
 * Maps between dashboard data models and domain entities.
 */
object DashboardMapper {

    fun DataSensorState.toDomain(): SensorState =
        SensorState(
            accel = accel,
            gyro = gyro,
            gravityAngle = gravityAngle,
            proximityNear = proximityNear,
            mlConfidence = mlConfidence
        )

    fun SensorState.toData(): DataSensorState =
        DataSensorState(
            accel = accel,
            gyro = gyro,
            gravityAngle = gravityAngle,
            proximityNear = proximityNear,
            mlConfidence = mlConfidence
        )

    fun DataDetectionResult.toDomain(): DetectionResult = when (this) {
        is DataDetectionResult.Crash -> DetectionResult.Crash
        is DataDetectionResult.Snatch -> DetectionResult.Snatch
        is DataDetectionResult.None -> DetectionResult.None
    }

    fun DetectionResult.toData(): DataDetectionResult = when (this) {
        is DetectionResult.Crash -> DataDetectionResult.Crash
        is DetectionResult.Snatch -> DataDetectionResult.Snatch
        is DetectionResult.None -> DataDetectionResult.None
    }

    fun DataDashboardStatus.toDomain(): DashboardStatus =
        DashboardStatus(audio = audio, crash = crash, snatch = snatch)

    fun DashboardStatus.toData(): DataDashboardStatus =
        DataDashboardStatus(audio = audio, crash = crash, snatch = snatch)

    fun DataEmergencyContact.toDomain(): EmergencyContact =
        EmergencyContact(name = name, phone = phone)

    fun EmergencyContact.toData(): DataEmergencyContact =
        DataEmergencyContact(name = name, phone = phone)
}
