package com.example.emergencyresponder.modules.dashboard.domain.engine

import com.example.emergencyresponder.modules.dashboard.domain.model.DetectionResult
import com.example.emergencyresponder.modules.dashboard.domain.model.SensorState
import com.example.emergencyresponder.modules.dashboard.domain.usecase.CrashDetectionUseCase

class CrashDetectionEngine(
    private val crashPredictor: CrashPredictor,
    private val useCase: CrashDetectionUseCase
) {
    fun evaluate(state: SensorState, features: FloatArray): DetectionResult {
        val confidence = crashPredictor.predict(features)
        return useCase.process(state.copy(mlConfidence = confidence))
    }
}
