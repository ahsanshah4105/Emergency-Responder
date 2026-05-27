package com.example.emergencyresponder.modules.dashboard.domain.usecase

import com.example.emergencyresponder.modules.dashboard.domain.model.DetectionResult
import com.example.emergencyresponder.modules.dashboard.domain.model.SensorState
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CrashDetectionUseCaseTest {

    private lateinit var useCase: CrashDetectionUseCase

    @Before
    fun setup() {
        useCase = CrashDetectionUseCase(Sensitivity.MEDIUM)
    }

    @Test
    fun `process returns None when mlConfidence below threshold`() {
        val state = SensorState(
            accel = 10.0,
            gyro = 5.0,
            gravityAngle = 20.0,
            proximityNear = true,
            mlConfidence = 0.5f // below MEDIUM threshold 0.75
        )
        val result = useCase.process(state)
        Assert.assertEquals(DetectionResult.None, result)
    }

    @Test
    fun `process returns Crash when mlConfidence above threshold and rules pass`() {
        val state = SensorState(
            accel = 40.0,  // above MEDIUM crashAccel * scale
            gyro = 10.0,   // above MEDIUM gyro * scale
            gravityAngle = 35.0,
            proximityNear = true,
            mlConfidence = 0.8f
        )
        val result = useCase.process(state)
        Assert.assertEquals(DetectionResult.Crash, result)
    }
}
