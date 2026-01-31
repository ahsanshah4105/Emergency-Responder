package com.example.emergencyresponder.modules.dashboard.domain.notifier


import android.content.Context
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceAlertManager(context: Context) : TextToSpeech.OnInitListener {

    //private var tts: TextToSpeech = TextToSpeech(context, this)
    var remainingSeconds: Long = 60 // default countdown
    private var timer: CountDownTimer? = null

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
    fun startCrashCountdown(onFinish: () -> Unit) {
        speak(
            "A crash has been detected. " +
                    "If you are safe, please cancel the alert within 60 seconds. " +
                    "Otherwise, an emergency message will be sent automatically."
        )

        CrashCountdownManager.startCountdown(
            onTick = { sec ->
                remainingSeconds = sec
                // 🔊 speak at important intervals
                when (sec) {
                    60L,50L,40L,30L,20L,10L -> speak("$sec seconds remaining")
                    in 1..5 -> speak(sec.toString())
                }
            },
            onFinish = {
                // countdown finished, you can update UI if needed
                remainingSeconds = 0
            },
            finalAction = onFinish // only triggered if not cancelled
        )
    }


    fun shutdown() {
        tts?.shutdown()
        timer?.cancel()

    }
}
