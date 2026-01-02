package com.example.emergencyresponder.modules.dashboard.domain.useCase

import com.example.emergencyresponder.modules.dashboard.data.model.AudioState


class AudioAnalysisUseCase {

    private val IMPACT_DB_THRESHOLD = 85
    private val SPIKE_DELTA = 25

    fun analyze(
        decibel: Int,
        delta: Int
    ): AudioState {
        return AudioState(
            loudImpact = decibel > IMPACT_DB_THRESHOLD,
            suddenSpike = delta > SPIKE_DELTA
        )
    }
}
