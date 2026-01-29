package com.example.emergencyresponder.modules.dashboard.ui.service

import Sensitivity
import android.app.Service
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.emergencyresponder.core.utils.SOSUtils
import com.example.emergencyresponder.modules.dashboard.data.ml.TFLiteCrashMlAnalyzer
import com.example.emergencyresponder.modules.dashboard.data.model.DetectionResult
import com.example.emergencyresponder.modules.dashboard.data.sensor.AndroidSensorProvider
import com.example.emergencyresponder.modules.dashboard.data.sensor.SensorProvider
import com.example.emergencyresponder.modules.dashboard.domain.engine.CrashDetectionEngine
import com.example.emergencyresponder.modules.dashboard.domain.engine.FeatureExtractor
import com.example.emergencyresponder.modules.dashboard.domain.notifier.AlertNotifier
import com.example.emergencyresponder.modules.dashboard.domain.notifier.AndroidAlertNotifier
import com.example.emergencyresponder.modules.dashboard.domain.notifier.VoiceAlertManager
import com.example.emergencyresponder.modules.dashboard.domain.useCase.CrashDetectionUseCase


class CrashDetectionService : Service() {
    private val featureExtractor = FeatureExtractor()

    private lateinit var sensorProvider: SensorProvider
    private lateinit var engine: CrashDetectionEngine
    private lateinit var notifier: AlertNotifier

    lateinit var voiceManager: VoiceAlertManager


    override fun onCreate() {
        super.onCreate()

        sensorProvider = AndroidSensorProvider(this)
        notifier = AndroidAlertNotifier(this)
        voiceManager = VoiceAlertManager(this)
        engine = CrashDetectionEngine(
            mlAnalyzer = TFLiteCrashMlAnalyzer(this),
            useCase = CrashDetectionUseCase(Sensitivity.HIGH)
        )

        sensorProvider.start { state ->
            val features = featureExtractor.extract(state) ?: return@start

            val result = engine.evaluate(state, features)

            sensorProvider.start { state ->
                val features = featureExtractor.extract(state) ?: return@start
                val result = engine.evaluate(state, features)

                when (result) {
                    DetectionResult.Crash -> {
                        notifier.notifyCrash()

                        // 2️⃣ Start voice countdown asynchronously
                        voiceManager.startCrashCountdown {
                            SOSUtils.sendSOSOnWhatsApp(this, "+923068988678") // send SOS automatically
                        }
                    }
                    DetectionResult.Snatch -> {
                        voiceManager.speak("Phone snatching detected!")
                        notifier.notifySnatch()
                    }
                    DetectionResult.None -> Unit
                }
            }

        }


        startForegroundInternal()
    }

    override fun onDestroy() {
        sensorProvider.stop()
        voiceManager.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private fun startForegroundInternal() {
        val channelId = "safety_foreground"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Emergency Monitoring",
                android.app.NotificationManager.IMPORTANCE_LOW
            )
            val manager =
                getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.example.emergencyresponder.R.drawable.app_logo)
            .setContentTitle("Emergency Monitor Active")
            .setContentText("Monitoring motion sensors")
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val triggeredByPower = intent?.getBooleanExtra("TRIPLE_POWER_PRESS", false) ?: false

        if (triggeredByPower) {

            voiceManager.speak("Emergency shortcut activated.")

            voiceManager.startCrashCountdown {
                notifier.notifyCrash()
            }
        }

        return START_STICKY
    }

}
