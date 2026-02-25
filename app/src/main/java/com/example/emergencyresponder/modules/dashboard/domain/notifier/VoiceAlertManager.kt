package com.example.emergencyresponder.modules.dashboard.domain.notifier


import android.content.Context
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale

class VoiceAlertManager(context: Context) : TextToSpeech.OnInitListener {

    var remainingSeconds: Long = 30 // default countdown
    private var timer: CountDownTimer? = null
    private var voiceJob: Job? = null
    private var tts: TextToSpeech? = null
    private var isReady = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setSpeechRate(1.0f)
                isReady = true
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            tts?.setSpeechRate(1.0f)
            isReady = true
        }
    }

    fun speak(text: String) {
        if (isReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "VOICE_ALERT")
        }
    }

    fun startVoiceMonitoring(scope: CoroutineScope, onSosTrigger: () -> Unit) {
        // First announcement
        speak(
            "Emergency detected. " +
                    "If you are safe, cancel within 30 seconds. " +
                    "Otherwise, an emergency message will be sent."
        )

        // Start the actual timer in the Manager
        CrashCountdownManager.startCountdown(onSosAction = onSosTrigger)

        // Observe the flow for voice intervals
        voiceJob?.cancel() // Clear any old monitoring
        voiceJob = scope.launch {
            CrashCountdownManager.remainingSeconds.collect { sec ->
                handleVoiceIntervals(sec)
            }
        }
    }
    private fun handleVoiceIntervals(sec: Long) {
        when (sec) {
            20L, 10L -> speak("$sec seconds remaining")
            in 1L..5L -> speak(sec.toString()) // 5, 4, 3, 2, 1
        }
    }
    fun shutdown() {
        voiceJob?.cancel()
        tts?.stop()
        tts?.shutdown()
        isReady = false
    }
}
