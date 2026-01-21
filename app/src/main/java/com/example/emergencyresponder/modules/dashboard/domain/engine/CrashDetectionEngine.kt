package com.example.emergencyresponder.modules.dashboard.domain.engine

import com.example.emergencyresponder.modules.dashboard.data.ml.CrashMlAnalyzer
import com.example.emergencyresponder.modules.dashboard.data.model.DetectionResult
import com.example.emergencyresponder.modules.dashboard.data.model.SensorState
import com.example.emergencyresponder.modules.dashboard.domain.useCase.CrashDetectionUseCase

class CrashDetectionEngine(
    private val mlAnalyzer: CrashMlAnalyzer,
    private val useCase: CrashDetectionUseCase
) {
    fun evaluate(state: SensorState, features: FloatArray): DetectionResult {
        val confidence = mlAnalyzer.predict(features)
        return useCase.process(state.copy(mlConfidence = confidence))
    }
}
