package com.example.emergencyresponder.modules.dashboard.data.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.emergencyresponder.core.base.BaseForegroundService
import com.example.emergencyresponder.core.base.EmergencyResponderApp
import com.example.emergencyresponder.core.data.local.UserPreferencesManager
import com.example.emergencyresponder.core.utils.NotificationHelper
import com.example.emergencyresponder.modules.dashboard.domain.notifier.VoiceAlertManager
import com.example.emergencyresponder.modules.dashboard.domain.usecase.AudioAnalysisUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MicListenService : BaseForegroundService() {

    override val notificationId = NOTIFICATION_ID
    override val notificationTitle = "Safety Monitor Active"
    override val notificationContent = "Listening for emergency signals..."

    private lateinit var audioUseCase: AudioAnalysisUseCase
    private lateinit var prefProvider: UserPreferencesManager
    private lateinit var voiceManager: VoiceAlertManager

    private val serviceJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + serviceJob)

    private var audioRecord: AudioRecord? = null
    private var isRunning = false
    private var detecting = true

    companion object {
        private const val NOTIFICATION_ID = 202
        private const val ALARM_ID = 99
        private const val FRAME_SIZE = 15600
        private const val SAMPLE_RATE = 16000
    }

    override fun onCreate() {
        super.onCreate()
        val container = (application as EmergencyResponderApp).appContainer
        audioUseCase = container.audioAnalysisUseCase
        voiceManager = container.voiceAlertManager
        prefProvider = container.userPrefs
        startListening()
    }

    private fun startListening() {
        if (isRunning) return
        isRunning = true

        scope.launch {
            try {
                audioRecord = withContext(Dispatchers.IO) {
                    createAudioRecord(FRAME_SIZE)
                }

                audioRecord?.let { record ->
                    if (record.state != android.media.AudioRecord.STATE_INITIALIZED) {
                        Log.e("MicListenService", "Hardware initialization failed")
                        return@launch
                    }

                    record.startRecording()
                    runDetectionLoop(record)
                }
            } catch (e: Exception) {
                Log.e("MicListenService", "Error in detection: ${e.message}")
            }
        }
    }

    private suspend fun runDetectionLoop(record: android.media.AudioRecord) {
        val audioBuffer = ShortArray(FRAME_SIZE)
        val windowSize = 3
        val clapWindow = ArrayDeque<Float>(windowSize)
        val whistleWindow = ArrayDeque<Float>(windowSize)
        var lastTriggerTime = 0L

        Log.d("MicListenService", "🚀 Detection Loop Started - Buffer: $FRAME_SIZE")

        while (isRunning) {
            val read = record.read(audioBuffer, 0, FRAME_SIZE)

            if (read > 0 && detecting) {
                val rms = calculateRMS(audioBuffer)

                if (rms < 400) {
                    continue
                }

                val floatBuffer = FloatArray(FRAME_SIZE) { audioBuffer[it] / 32768f }
                val (rawClap, rawWhistle) = audioUseCase.analyzeRawScores(floatBuffer)

                updateWindow(clapWindow, rawClap, windowSize)
                updateWindow(whistleWindow, rawWhistle, windowSize)

                val maxClap = clapWindow.maxOrNull() ?: 0f
                val maxWhistle = whistleWindow.maxOrNull() ?: 0f

                Log.d(
                    "MicDetection",
                    "📊 [LIVE] RMS: ${rms.toInt()} | Whistle Raw: $rawWhistle | Whistle Max(Window): $maxWhistle | Clap Max: $maxClap"
                )

                if (audioUseCase.isEmergencySound(clapWindow.toList(), whistleWindow.toList())) {
                    val now = System.currentTimeMillis()
                    val cooldownRemaining = 5000 - (now - lastTriggerTime)

                    if (now - lastTriggerTime > 5000) {
                        Log.i("MicDetection", "✅ TRIGGER: Sound matched threshold! Starting Alarm.")
                        lastTriggerTime = now
                        withContext(Dispatchers.Main) { triggerAlarm() }
                    } else {
                        Log.w(
                            "MicDetection",
                            "⏳ MATCHED but in COOLDOWN. Wait ${cooldownRemaining / 1000}s"
                        )
                    }
                }
            }
        }
    }

    private fun updateWindow(window: ArrayDeque<Float>, value: Float, size: Int) {
        if (window.size >= size) window.removeFirst()
        window.addLast(value)
    }

    private fun triggerAlarm() {
        if (!detecting) return
        detecting = false

        scope.launch(Dispatchers.Main) {
            try {
                val name = prefProvider.getUserName() ?: "User"
                voiceManager.speak("Hi $name, I am here.")
                kotlinx.coroutines.delay(2500)
                voiceManager.speak("Hi $name, I am here.")

                val manager = getSystemService(NotificationManager::class.java)
                manager.notify(
                    ALARM_ID, NotificationHelper.getBaseNotification(
                        this@MicListenService,
                        "🚨 Clap/Whistle Detected",
                        "Emergency response triggered for $name."
                    )
                )

                kotlinx.coroutines.delay(1000)
            } finally {
                detecting = true
            }
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun createAudioRecord(size: Int): AudioRecord {
        val minBufSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(size)

        return AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufSize
        )
    }

    private fun calculateRMS(buffer: ShortArray): Double {
        var sum = 0.0
        for (s in buffer) sum += s * s.toDouble()
        return kotlin.math.sqrt(sum / buffer.size)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    @SuppressLint("ServiceCast")
    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)

        val restartServicePendingIntent = PendingIntent.getService(
            this, 1, restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmService = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 1000,
            restartServicePendingIntent
        )

        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        isRunning = false
        serviceJob.cancel()
        audioRecord?.apply {
            try {
                stop()
            } catch (e: Exception) {
            }
            release()
        }
        audioRecord = null
        super.onDestroy()
    }
}