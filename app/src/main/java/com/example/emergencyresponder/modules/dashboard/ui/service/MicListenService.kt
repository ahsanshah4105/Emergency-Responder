package com.example.emergencyresponder.modules.dashboard.ui.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.audiofx.NoiseSuppressor
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.emergencyresponder.modules.dashboard.domain.notifier.VoiceAlertManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private lateinit var voiceManager: VoiceAlertManager

    private val sampleRate = 16000
    private val frameSize = 15600 // YAMNet input size (0.975 sec)

    private var clapIndex = -1
    private var whistleIndex = -1
    private val windowSize = 8
    private val clapWindow = ArrayDeque<Float>()
    private val whistleWindow = ArrayDeque<Float>()

    private var clapStartTime = 0L
    private var whistleStartTime = 0L
    private val stableDuration = 1000L  // 2 seconds

    // <-- ADD THIS FLAG
    private var detecting = true
    override fun onCreate() {
        super.onCreate()
        if (!hasAudioPermission()) {
            stopSelf()
            return
        }
        voiceManager = VoiceAlertManager(this)


    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("MicListenService", "✅ Service Started Running")
        startForegroundNotification()
        if (!checkMicPermissions()) {
            stopSelf()
            return START_NOT_STICKY
        }

        // MUST be first

        // load model in background
        CoroutineScope(Dispatchers.IO).launch {
            loadModel()
            loadLabelIndices()
            startListening()
        }


        return START_STICKY
    }


    // ---------------- MODEL ----------------

    private fun loadModel() {
        val model = loadModelFile("yamnet.tflite")
        interpreter = Interpreter(model)

        // IMPORTANT: allocate tensors before running inference
        interpreter.allocateTensors()

        // OPTIONAL: print input/output shapes to verify
        val inputShape = interpreter.getInputTensor(0).shape()
        val outputShape = interpreter.getOutputTensor(0).shape()
        Log.d("MicListenService", "Input shape: ${inputShape.contentToString()}")
        Log.d("MicListenService", "Output shape: ${outputShape.contentToString()}")
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

        if (NoiseSuppressor.isAvailable()) {
            NoiseSuppressor.create(audioRecord!!.audioSessionId)
        }


        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("MicListenService", "AudioRecord initialization failed")
            stopSelf()
            return
        }

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
                    val rms = calculateRMS(audioBuffer)

                    Log.d("MicListenService", "RMS = $rms")

// Ignore only very silent frames
                    if (rms < 600) {
                        Log.d("MicListenService", "Weak sound, skipping...")
                        continue
                    }



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



                    // ✅ Fixed low threshold detection
                    val clapDetected = clapAvg >= 0.05f
                    val whistleDetected = whistleAvg >= 0.05f



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

    private fun calculateRMS(buffer: ShortArray): Float {
        var sum = 0.0
        for (s in buffer) {
            sum += s * s.toDouble()
        }
        return kotlin.math.sqrt(sum / buffer.size).toFloat()
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
        voiceManager.speak("Hi, I am here")
    }




    private fun checkMicPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_MICROPHONE) == PackageManager.PERMISSION_GRANTED
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }


    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() {
        Log.d("MicListenService", "❌ Service Destroyed / Stopped")

        running = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        voiceManager.shutdown()

        super.onDestroy()

    }
}



