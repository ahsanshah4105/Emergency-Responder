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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.abs

class MicListenService : Service() {

    private var audioRecord: AudioRecord? = null
    private var running = false
    private lateinit var interpreter: Interpreter

    private val sampleRate = 16000
    private val frameSize = 15600 // YAMNet input size (0.975 sec)

    private var clapIndex = -1
    private var whistleIndex = -1
    private val windowSize = 4
    private val clapWindow = ArrayDeque<Float>()
    private val whistleWindow = ArrayDeque<Float>()

    private var clapStartTime = 0L
    private var whistleStartTime = 0L
    private val stableDuration = 2000L  // 2 seconds

    // <-- ADD THIS FLAG
    private var detecting = true
    override fun onCreate() {
        super.onCreate()

        if (!hasAudioPermission()) {
            stopSelf()
            return
        }

        loadModel()
        loadLabelIndices()
        startForegroundNotification()
        startListening()
    }

    // ---------------- MODEL ----------------

    private fun loadModel() {
        val model = loadModelFile("yamnet.tflite")
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

    // ---------------- LABELS ----------------

    private fun loadLabelIndices() {
        val reader = BufferedReader(InputStreamReader(assets.open("yamnet_class_map.csv")))
        reader.readLine() // skip header

        var index = 0
        reader.forEachLine { line ->
            val parts = line.split(",")
            val displayName = parts[2].trim()

            if (displayName.equals("Whistle", true)) {
                whistleIndex = index
            }

            if (displayName.equals("Clapping", true)) {
                clapIndex = index
            }

            index++
        }

        reader.close()
        Log.d("MicListenService", "ClapIndex=$clapIndex WhistleIndex=$whistleIndex")
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

                    // <-- SKIP DETECTION WHEN SIREN IS PLAYING
                    if (!detecting) continue

                    val inputTensor = Array(1) { FloatArray(frameSize) }
                    for (i in 0 until frameSize) {
                        inputTensor[0][i] = audioBuffer[i] / 32768f
                    }

                    val output = Array(1) { FloatArray(521) }
                    interpreter.run(inputTensor, output)

                    val clapScore = output[0][clapIndex]
                    val whistleScore = output[0][whistleIndex]

                    Log.d("MicListenService", "YAMNet → Clap=$clapScore Whistle=$whistleScore")

                    clapWindow.addLast(clapScore)
                    whistleWindow.addLast(whistleScore)

                    if (clapWindow.size > windowSize) clapWindow.removeFirst()
                    if (whistleWindow.size > windowSize) whistleWindow.removeFirst()

                    val clapAvg = clapWindow.average().toFloat()
                    val whistleAvg = whistleWindow.average().toFloat()

                    val clapDetected = clapAvg > 0.15f && clapAvg > whistleAvg + 0.05f
                    val whistleDetected = whistleAvg > 0.10f && whistleAvg > clapAvg + 0.05f

                    val currentTime = System.currentTimeMillis()

// clap stability timer
                    if (clapDetected) {
                        if (clapStartTime == 0L) clapStartTime = currentTime
                    } else {
                        clapStartTime = 0L
                    }

// whistle stability timer
                    if (whistleDetected) {
                        if (whistleStartTime == 0L) whistleStartTime = currentTime
                    } else {
                        whistleStartTime = 0L
                    }

// if stable for 2 seconds then trigger
                    val stableClap = clapStartTime != 0L && (currentTime - clapStartTime >= stableDuration)
                    val stableWhistle = whistleStartTime != 0L && (currentTime - whistleStartTime >= stableDuration)

                    val confident = stableClap || stableWhistle

                    val cooldownPassed = System.currentTimeMillis() - lastTriggerTime > 2000

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

        // 🔥 STOP detection while siren plays
        detecting = false
        clapWindow.clear()
        whistleWindow.clear()

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

        // 🔥 RESTART detection after siren stops
        Handler(Looper.getMainLooper()).postDelayed({
            detecting = true
        }, 500)
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
