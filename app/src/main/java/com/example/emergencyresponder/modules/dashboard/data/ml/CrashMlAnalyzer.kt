package com.example.emergencyresponder.modules.dashboard.data.ml

interface CrashMlAnalyzer {
    fun predict(features: FloatArray): Float
}
