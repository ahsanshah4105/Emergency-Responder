package com.example.emergencyresponder.modules.dashboard.domain.notifier


import android.content.Context
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceAlertManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech = TextToSpeech(context, this)

    private var isReady = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
            tts.setSpeechRate(1.0f)
            isReady = true
        }
    }

    fun speak(text: String) {
        if (!isReady) return

        tts.stop()
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun startCrashCountdown(onFinish: () -> Unit) {

        speak(
            "A crash has been detected. " +
                    "If you are safe, please cancel the alert within 60 seconds. " +
                    "Otherwise, an emergency message will be sent automatically."
        )

        object : CountDownTimer(60000, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                val sec = millisUntilFinished / 1000

                // 🔥 Speak only at important intervals
                when {
                    sec == 60L -> speak("60 seconds remaining")
                    sec == 50L -> speak("50 seconds remaining")
                    sec == 40L -> speak("40 seconds remaining")
                    sec == 30L -> speak("30 seconds remaining")
                    sec == 20L -> speak("20 seconds remaining")
                    sec == 10L -> speak("10 seconds remaining")

                    // Final countdown voice
                    sec in 1..5 -> speak(sec.toString())
                }
            }

            override fun onFinish() {
                speak(
                    "No response was received. " +
                            "An emergency alert is now being sent to your trusted contacts. " +
                            "Help is on the way."
                )
                onFinish()
            }

        }.start()
    }

    fun shutdown() {
        tts.shutdown()
    }
}
