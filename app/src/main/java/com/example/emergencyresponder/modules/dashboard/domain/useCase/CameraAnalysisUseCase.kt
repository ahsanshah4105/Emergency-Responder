package com.example.emergencyresponder.modules.dashboard.domain.useCase

import com.example.emergencyresponder.modules.dashboard.data.model.CameraState
import kotlin.math.abs

class CameraAnalysisUseCase {

    private val MOTION_THRESHOLD = 0.65
    private val BLUR_THRESHOLD = 120.0
    private val DARK_THRESHOLD = 40

    fun analyze(
        frameDiff: Double,
        blurScore: Double,
        orientationDelta: Float,
        brightness: Int
    ): CameraState {

        return CameraState(
            suddenMotion = frameDiff > MOTION_THRESHOLD,
            heavyBlur = blurScore < BLUR_THRESHOLD,
            orientationFlip = abs(orientationDelta) > 60f,
            darkness = brightness < DARK_THRESHOLD
        )
    }
}
