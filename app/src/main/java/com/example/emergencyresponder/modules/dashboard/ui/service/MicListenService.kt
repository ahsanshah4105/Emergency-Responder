package com.example.emergencyresponder.modules.dashboard.ui.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import org.jtransforms.fft.FloatFFT_1D


class MicListenService : Service() {

    private var audioRecord: AudioRecord? = null
    private var running = false
    private lateinit var interpreter: Interpreter

    private val sampleRate = 22050
    private val frameSize = 22050 // 1 second

    override fun onCreate() {
        super.onCreate()

        if (!hasAudioPermission()) {
            stopSelf()
            return
        }

        loadModel()
        startForegroundNotification()
        startListening()
    }

    // ---------------- MODEL ----------------

    private fun loadModel() {
        val model = loadModelFile("clap_whistle_raw_model.tflite")
        interpreter = Interpreter(model)
    }


    private fun loadModelFile(modelName: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelName)
        val inputStream = fileDescriptor.createInputStream()
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    // ---------------- AUDIO ----------------

    private fun startListening() {
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(frameSize)

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        audioRecord?.startRecording()
        running = true

        Thread {
            val audioBuffer = ShortArray(frameSize)
            var offset = 0
            var lastTriggerTime = 0L

            while (running) {
                val read = audioRecord?.read(audioBuffer, offset, frameSize - offset) ?: 0
                if (read <= 0) continue

                offset += read

                if (offset >= frameSize) {
                    offset = 0

                    // ✅ RAW audio tensor (1, 22050)
                    val inputTensor = Array(1) { FloatArray(frameSize) }
                    for (i in 0 until frameSize) {
                        inputTensor[0][i] = audioBuffer[i] / 32768f
                    }

                    val output = Array(1) { FloatArray(2) }
                    interpreter.run(inputTensor, output)

                    val clapScore = output[0][0]
                    val whistleScore = output[0][1]

                    Log.d("MicListenService", "MODEL → Clap=$clapScore Whistle=$whistleScore")

                    val confident =
                        (clapScore > 0.85f || whistleScore > 0.85f) &&
                                kotlin.math.abs(clapScore - whistleScore) > 0.25f

                    val cooldownPassed =
                        System.currentTimeMillis() - lastTriggerTime > 2000

                    if (confident && cooldownPassed) {
                        lastTriggerTime = System.currentTimeMillis()
                        triggerAlarm()
                    }
                }
            }
        }.start()
    }

    // ---------------- PERMISSIONS ----------------

    private fun hasAudioPermission(): Boolean {
        val mic = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.RECORD_AUDIO
        )

        val fgsMic = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.FOREGROUND_SERVICE_MICROPHONE
            )
        } else PackageManager.PERMISSION_GRANTED

        return mic == PackageManager.PERMISSION_GRANTED &&
                fgsMic == PackageManager.PERMISSION_GRANTED
    }

    // ---------------- FOREGROUND ----------------

    private fun startForegroundNotification() {
        val channelId = "mic_listen_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Mic Listen",
                android.app.NotificationManager.IMPORTANCE_LOW
            )
            val manager =
                getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.example.emergencyresponder.R.drawable.app_logo)
            .setContentTitle("Listening for Clap / Whistle")
            .setContentText("ML model running")
            .setOngoing(true)
            .build()

        startForeground(2, notification)
    }

    // ---------------- ALARM ----------------

    private fun triggerAlarm() {
        Log.d("MicListenService", "🚨 MODEL TRIGGERED")

        playSiren()

        val channelId = "alarm_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Alarm",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            val manager =
                getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.example.emergencyresponder.R.drawable.app_logo)
            .setContentTitle("ALARM ACTIVE")
            .setContentText("Clap or whistle detected")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(99, notification)
    }

    private fun playSiren() {
        val player = MediaPlayer.create(this, com.example.emergencyresponder.R.raw.siren)
        player.start()

        Handler(Looper.getMainLooper()).postDelayed({
            player.stop()
            player.release()
        }, 10000)
    }

    // ---------------- LIFECYCLE ----------------

    override fun onDestroy() {
        running = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}