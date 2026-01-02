package com.example.emergencyresponder.modules.dashboard.ui.service


import android.media.MediaRecorder
import com.example.emergencyresponder.modules.dashboard.data.model.AudioState
import com.example.emergencyresponder.modules.dashboard.domain.useCase.AudioAnalysisUseCase
import kotlin.math.log10

class AudioMonitor(
    private val onResult: (AudioState) -> Unit
) {

    private var recorder: MediaRecorder? = null
    private val audioUseCase = AudioAnalysisUseCase()
    private var lastDb = 0

    fun start() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile("/dev/null")
            prepare()
            start()
        }

        Thread {
            repeat(10) {
                val amp = recorder?.maxAmplitude ?: 0
                val db = if (amp > 0) (20 * log10(amp.toDouble())).toInt() else 0
                val delta = kotlin.math.abs(db - lastDb)
                lastDb = db

                onResult(audioUseCase.analyze(db, delta))
                Thread.sleep(150)
            }
            stop()
        }.start()
    }

    fun stop() {
        recorder?.release()
        recorder = null
    }
}
