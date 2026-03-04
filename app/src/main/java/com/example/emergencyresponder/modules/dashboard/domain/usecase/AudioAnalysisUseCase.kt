package com.example.emergencyresponder.modules.dashboard.domain.usecase

import com.example.emergencyresponder.modules.dashboard.data.provider.YAMNetClassifier

class AudioAnalysisUseCase(private val classifier: YAMNetClassifier) {

    companion object {
        private const val CLAP_THRESHOLD = 0.4f
        private const val WHISTLE_THRESHOLD = 0.15f
        const val LABEL_CLAP = "Clapping"
        const val LABEL_WHISTLE = "Whistle"
    }

    fun isEmergencySound(clapWindow: List<Float>, whistleWindow: List<Float>): Boolean {
        val maxClap = clapWindow.maxOrNull() ?: 0f
        val maxWhistle = whistleWindow.maxOrNull() ?: 0f

        return maxClap >= CLAP_THRESHOLD || maxWhistle >= WHISTLE_THRESHOLD
    }

    fun analyzeRawScores(audioData: FloatArray): Pair<Float, Float> {
        val results = classifier.classify(audioData)
        val clapIdx = classifier.getIndexFor(LABEL_CLAP)
        val whistleIdx = classifier.getIndexFor(LABEL_WHISTLE)

        return Pair(
            if (clapIdx != -1) results[clapIdx] else 0f,
            if (whistleIdx != -1) results[whistleIdx] else 0f
        )
    }
}