package com.example.emergencyresponder.modules.dashboard.ui.service

import Sensitivity
import android.app.Service
import android.content.Context
import android.content.Intent
import com.example.emergencyresponder.modules.dashboard.data.ml.TFLiteCrashMlAnalyzer
import com.example.emergencyresponder.modules.dashboard.data.model.DetectionResult
import com.example.emergencyresponder.modules.dashboard.data.sensor.AndroidSensorProvider
import com.example.emergencyresponder.modules.dashboard.data.sensor.SensorProvider
import com.example.emergencyresponder.modules.dashboard.domain.engine.CrashDetectionEngine
import com.example.emergencyresponder.modules.dashboard.domain.engine.FeatureExtractor
import com.example.emergencyresponder.modules.dashboard.domain.notifier.AlertNotifier
import com.example.emergencyresponder.modules.dashboard.domain.notifier.AndroidAlertNotifier
import com.example.emergencyresponder.modules.dashboard.domain.useCase.CrashDetectionUseCase


class CrashDetectionService : Service() {
    private val featureExtractor = FeatureExtractor()

    private lateinit var sensorProvider: SensorProvider
    private lateinit var engine: CrashDetectionEngine
    private lateinit var notifier: AlertNotifier

    override fun onCreate() {
        super.onCreate()

        sensorProvider = AndroidSensorProvider(this)
        notifier = AndroidAlertNotifier(this)

        engine = CrashDetectionEngine(
            mlAnalyzer = TFLiteCrashMlAnalyzer(this),
            useCase = CrashDetectionUseCase(Sensitivity.HIGH)
        )

        sensorProvider.start { state ->
            val features = featureExtractor.extract(state) ?: return@start

            val result = engine.evaluate(state, features)

            when (result) {
                DetectionResult.Crash -> notifier.notifyCrash()
                DetectionResult.Snatch -> notifier.notifySnatch()
                DetectionResult.None -> Unit
            }
        }


        startForegroundInternal()
    }

    override fun onDestroy() {
        sensorProvider.stop()
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

}
