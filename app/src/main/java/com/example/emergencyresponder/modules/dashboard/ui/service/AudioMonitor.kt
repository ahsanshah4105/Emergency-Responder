package com.example.emergencyresponder.modules.dashboard.ui.service

import android.R
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlin.math.abs
import kotlin.math.log10

class MicListenService : Service() {

    private var audioRecord: AudioRecord? = null
    private var running = false

    override fun onCreate() {
        super.onCreate()

        // Start foreground only if permission exists
        if (!hasAudioPermission()) {
            stopSelf()
            return
        }

        startForegroundServiceNotification()
        startListening()
    }

    override fun onDestroy() {
        running = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun hasAudioPermission(): Boolean {
        val record = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.RECORD_AUDIO
        )

        val fgsMic = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.FOREGROUND_SERVICE_MICROPHONE
            )
        } else {
            PackageManager.PERMISSION_GRANTED
        }

        return record == PackageManager.PERMISSION_GRANTED && fgsMic == PackageManager.PERMISSION_GRANTED
    }

    private fun startForegroundServiceNotification() {
        val channelId = "mic_listen_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Mic Listen Service",
                android.app.NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.example.emergencyresponder.R.drawable.app_logo)
            .setContentTitle("Listening for Clap / Whistle")
            .setContentText("Mic service running")
            .setOngoing(true)
            .build()

        startForeground(2, notification)
    }

    private fun startListening() {

        if (!hasAudioPermission()) {
            stopSelf()
            return
        }

        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        try {
            audioRecord?.startRecording()
        } catch (e: SecurityException) {
            stopSelf()
            return
        }

        running = true

        Thread {
            val buffer = ShortArray(bufferSize)
            var clapCooldown = 0

            var lastTriggerTime = 0L // in milliseconds

            while (running) {
                val read = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                if (read > 0) {

                    val maxAmp = buffer.maxOrNull()?.toInt() ?: 0
                    val avgAmp = buffer.map { abs(it.toInt()) }.average()

                    val currentTime = System.currentTimeMillis()

                    if (maxAmp > 12000 && avgAmp > 2000 && (currentTime - lastTriggerTime) > 2000) {
                        lastTriggerTime = currentTime
                        Log.d("MicListenService", "Clap Detected")
                        triggerAlarm()
                    }
                }
            }

        }.start()
    }

    private fun triggerAlarm() {
        Log.d("MicListenService", "ALARM TRIGGERED!")

        // 1) Play sound
//        val alarmSound = android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
//        val ringtone = android.media.RingtoneManager.getRingtone(this, alarmSound)
//        ringtone.play()
        playSiren()

        // 2) Show notification
        val channelId = "alarm_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Alarm",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.example.emergencyresponder.R.drawable.app_logo)
            .setContentTitle("ALARM ACTIVE")
            .setContentText("Clap detected! Alarm triggered.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(99, notification)
    }

    private fun playSiren() {
        val mediaPlayer = MediaPlayer.create(this, com.example.emergencyresponder.R.raw.siren)
        mediaPlayer.start()

        // Stop after 10 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            mediaPlayer.stop()
            mediaPlayer.release()
        }, 10000)
    }

}
