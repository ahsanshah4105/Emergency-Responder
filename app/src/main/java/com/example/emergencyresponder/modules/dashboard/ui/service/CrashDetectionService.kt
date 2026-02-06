package com.example.emergencyresponder.modules.dashboard.ui.service

import Sensitivity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.emergencyresponder.core.utils.SOSUtils
import com.example.emergencyresponder.modules.dashboard.data.ml.TFLiteCrashMlAnalyzer
import com.example.emergencyresponder.modules.dashboard.data.model.DetectionResult
import com.example.emergencyresponder.modules.dashboard.data.provider.AndroidSensorProvider
import com.example.emergencyresponder.modules.dashboard.domain.repository.SensorProvider
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

    private val EMERGENCY_NUMBER = "+923199596412"
    private var crashAlreadyDetected = false

    override fun onCreate() {
        super.onCreate()

        // Initialize dependencies
        sensorProvider = AndroidSensorProvider(this)
        notifier = AndroidAlertNotifier(this)
        voiceManager = VoiceAlertManager(this)

        engine = CrashDetectionEngine(
            mlAnalyzer = TFLiteCrashMlAnalyzer(this),
            useCase = CrashDetectionUseCase(Sensitivity.HIGH)
        )

        // ✅ Start sensor monitoring only ONCE
        startCrashMonitoring()

        // Start foreground notification
        startForegroundInternal()
    }

    private fun startCrashMonitoring() {

        sensorProvider.start { state ->

            val features = featureExtractor.extract(state) ?: return@start
            val result = engine.evaluate(state, features)

            when (result) {

                DetectionResult.Crash -> {

                    // ✅ Prevent repeated crash alerts
                    if (crashAlreadyDetected) return@start
                    crashAlreadyDetected = true

                    Log.d("CrashService", "Crash detected!")

                    // Show notification
                    notifier.notifyCrash()

                    // Start countdown + auto SOS
                    voiceManager.startCrashCountdown {

                        Log.d("SOS", "Countdown finished → Sending SOS now")

                        SOSUtils.sendSOSViaSMS(
                            this@CrashDetectionService,
                            EMERGENCY_NUMBER
                        )

                        SOSUtils.sendSOSViaGreenApi(
                            this@CrashDetectionService,
                            EMERGENCY_NUMBER
                        )

                        // Optional WhatsApp fallback
                        // SOSUtils.sendSOSOnWhatsApp(this@CrashDetectionService, "923068988678")
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val triggeredByPower =
            intent?.getBooleanExtra("TRIPLE_POWER_PRESS", false) ?: false

        if (triggeredByPower) {

            voiceManager.speak("Emergency shortcut activated.")

            voiceManager.startCrashCountdown {

                Log.d("SOS", "Power shortcut countdown finished → Sending SOS")

                SOSUtils.sendSOSViaSMS(
                    this@CrashDetectionService,
                    EMERGENCY_NUMBER
                )

                // 2. Send WhatsApp via Green API (🔥 NEW ADDED)
                SOSUtils.sendSOSViaGreenApi(
                    this@CrashDetectionService,
                    EMERGENCY_NUMBER
                )
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        sensorProvider.stop()
        voiceManager.shutdown()

        Log.d("CrashService", "Service destroyed")
    }

    override fun onBind(intent: Intent?) = null

    // ✅ Foreground notification
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

        val notification =
            androidx.core.app.NotificationCompat.Builder(this, channelId)
                .setSmallIcon(com.example.emergencyresponder.R.drawable.app_logo)
                .setContentTitle("Emergency Monitor Active")
                .setContentText("Monitoring motion sensors for crashes")
                .setOngoing(true)
                .build()

        startForeground(1, notification)
    }
}
