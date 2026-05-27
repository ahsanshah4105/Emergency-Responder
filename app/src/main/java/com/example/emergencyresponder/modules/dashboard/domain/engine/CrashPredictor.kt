package com.example.emergencyresponder.modules.dashboard.domain.engine

/**
 * Domain abstraction for crash prediction from sensor features.
 * Implementations (e.g. TFLite) live in the data layer.
 */
interface CrashPredictor {
    fun predict(features: FloatArray): Float
}
