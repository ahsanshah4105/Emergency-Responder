//package com.example.emergencyresponder.modules.dashboard.ui.service
//
//import Sensitivity
//import android.app.Service
//import android.content.Context
//import android.content.Intent
//import android.os.Handler
//import android.os.Looper
//import android.os.PowerManager
//import android.util.Log
//import com.example.emergencyresponder.core.objects.SPreferenceManager
//import com.example.emergencyresponder.core.utils.SOSUtils
//import com.example.emergencyresponder.modules.dashboard.data.ml.TFLiteCrashMlAnalyzer
//import com.example.emergencyresponder.modules.dashboard.data.model.DetectionResult
//import com.example.emergencyresponder.modules.dashboard.data.provider.AndroidSensorProvider
//import com.example.emergencyresponder.modules.dashboard.domain.repository.SensorProvider
//import com.example.emergencyresponder.modules.dashboard.domain.engine.CrashDetectionEngine
//import com.example.emergencyresponder.modules.dashboard.domain.engine.FeatureExtractor
//import com.example.emergencyresponder.modules.dashboard.domain.notifier.AlertNotifier
//import com.example.emergencyresponder.modules.dashboard.domain.notifier.AndroidAlertNotifier
//import com.example.emergencyresponder.modules.dashboard.domain.notifier.VoiceAlertManager
//import com.example.emergencyresponder.modules.dashboard.domain.useCase.CrashDetectionUseCase
//
//class CrashDetectionService : Service() {
//
//    private val featureExtractor = FeatureExtractor()
//
//    private lateinit var sensorProvider: SensorProvider
//    private lateinit var engine: CrashDetectionEngine
//    private lateinit var notifier: AlertNotifier
//    lateinit var voiceManager: VoiceAlertManager
//    private var wakeLock: PowerManager.WakeLock? = null
//    private val EMERGENCY_NUMBER = "+923199596412"
//    private var crashAlreadyDetected = false
//
//    private var snatchAlreadyDetected = false
//
//    override fun onCreate() {
//        super.onCreate()
//        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
//        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EmergencyResponder:CrashMonitorWakelock")
//        wakeLock?.acquire()
//        sensorProvider = AndroidSensorProvider(this)
//        notifier = AndroidAlertNotifier(this)
//        voiceManager = VoiceAlertManager(this)
//
//
//
//        val savedSens = SPreferenceManager.getSensitivity() // Returns String "HIGH", "MEDIUM", "LOW"
//
//        val sensitivityEnum = when(savedSens) {
//            "HIGH" -> Sensitivity.HIGH
//            "MEDIUM" -> Sensitivity.MEDIUM
//            else -> Sensitivity.LOW
//        }
//        engine = CrashDetectionEngine(
//            mlAnalyzer = TFLiteCrashMlAnalyzer(this),
//            useCase = CrashDetectionUseCase(sensitivityEnum) // <--- Use the variable you calculated
//        )
//
//
//        startCrashMonitoring()
//
//        // Start foreground notification
//        startForegroundInternal()
//    }
//
////    private fun startCrashMonitoring() {
////
////        sensorProvider.start { state ->
////
////            val features = featureExtractor.extract(state) ?: return@start
////            val result = engine.evaluate(state, features)
////
////            when (result) {
////
////                DetectionResult.Crash -> {
////
////                    // ✅ Prevent repeated crash alerts
////                    if (crashAlreadyDetected) return@start
////                    crashAlreadyDetected = true
////
////                    Log.d("CrashService", "Crash detected!")
////
////                    // Show notification
////                    notifier.notifyCrash()
////
////                    // Start countdown + auto SOS
////                    voiceManager.startCrashCountdown {
////
////                        Log.d("SOS", "Countdown finished → Sending SOS now")
////
////                        SOSUtils.sendSOSViaSMS(
////                            this@CrashDetectionService,
////                            EMERGENCY_NUMBER
////                        )
////
////                        SOSUtils.sendSOSViaGreenApi(
////                            this@CrashDetectionService,
////                            EMERGENCY_NUMBER
////                        )
////
////                        // Optional WhatsApp fallback
////                        // SOSUtils.sendSOSOnWhatsApp(this@CrashDetectionService, "923068988678")
////                    }
////                }
////
////                DetectionResult.Snatch -> {
////                    voiceManager.speak("Phone snatching detected!")
////                    notifier.notifySnatch()
////                }
////
////                DetectionResult.None -> Unit
////            }
////        }
////    }
//private fun startCrashMonitoring() {
//    sensorProvider.start { state ->
//        val features = featureExtractor.extract(state) ?: return@start
//        val result = engine.evaluate(state, features)
//
//        when (result) {
//            DetectionResult.Crash -> {
//                if (crashAlreadyDetected) return@start
//
//                crashAlreadyDetected = true
//                Log.d("CrashService", "Crash detected! Locking.")
//
//                // ✅ 1. Update Notification Text to "Crash Detected"
//                notifier.notifyCrash()
//
//                voiceManager.startCrashCountdown {
//                    SOSUtils.sendSOSViaSMS(
//                            this@CrashDetectionService,
//                            EMERGENCY_NUMBER
//                        )
//
//                        SOSUtils.sendSOSViaGreenApi(
//                            this@CrashDetectionService,
//                            EMERGENCY_NUMBER
//                        )
//                }
//
//                // Reset lock after 15 seconds
//                Handler(Looper.getMainLooper()).postDelayed({
//                    crashAlreadyDetected = false
//                }, 15000)
//            }
//
//            DetectionResult.Snatch -> {
//                // Optional: Prevent Snatch Spam (e.g., 5 second cooldown)
//                if (snatchAlreadyDetected) return@start
//                snatchAlreadyDetected = true
//
//                voiceManager.speak("Phone snatching detected!")
//
//                // ✅ 2. Update Notification Text to "Snatch Detected"
//                notifier.notifySnatch()
//
//                Handler(Looper.getMainLooper()).postDelayed({
//                    snatchAlreadyDetected = false
//                }, 5000)
//            }
//
//            DetectionResult.None -> Unit
//        }
//    }
//}
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        val triggeredByPower = intent?.getBooleanExtra("TRIPLE_POWER_PRESS", false) ?: false
//
//        if (triggeredByPower) {
//            voiceManager.speak("Emergency shortcut activated.")
//
//            // ✅ 3. Update Notification Text to "SOS Activated"
//            notifier.notifyManualSOS()
//
//            voiceManager.startCrashCountdown {
//                Log.d("SOS", "Power shortcut countdown finished → Sending SOS")
//                SOSUtils.sendSOSViaSMS(this@CrashDetectionService, EMERGENCY_NUMBER)
//                SOSUtils.sendSOSViaGreenApi(this@CrashDetectionService, EMERGENCY_NUMBER)
//            }
//        }
//        return START_STICKY
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        sensorProvider.stop()
//        voiceManager.shutdown()
//        if (wakeLock?.isHeld == true) {
//            wakeLock?.release()
//        }
//    }
//
//    override fun onBind(intent: Intent?) = null
//
//    // ✅ Foreground notification
//    private fun startForegroundInternal() {
//
//        val channelId = "safety_foreground"
//
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//
//            val channel = android.app.NotificationChannel(
//                channelId,
//                "Emergency Monitoring",
//                android.app.NotificationManager.IMPORTANCE_LOW
//            )
//
//            val manager =
//                getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
//
//            manager.createNotificationChannel(channel)
//        }
//
//        val notification =
//            androidx.core.app.NotificationCompat.Builder(this, channelId)
//                .setSmallIcon(com.example.emergencyresponder.R.drawable.app_logo)
//                .setContentTitle("Emergency Monitor Active")
//                .setContentText("Monitoring motion sensors for crashes")
//                .setOngoing(true)
//                .build()
//
//        startForeground(1, notification)
//    }
//}
package com.example.emergencyresponder.modules.dashboard.ui.service

import Sensitivity
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import com.example.emergencyresponder.core.objects.SPreferenceManager
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
    private var wakeLock: PowerManager.WakeLock? = null
    private val EMERGENCY_NUMBER = "+923199596412"

    // --- FLAGS TO PREVENT SPAM ---
    private var crashAlreadyDetected = false
    private var snatchAlreadyDetected = false

    // --- POWER BUTTON LOGIC ---
    private var powerPressCount = 0
    private var lastPressTime = 0L

    // This receiver detects Screen ON / OFF events (Power Button presses)
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF || intent?.action == Intent.ACTION_SCREEN_ON) {
                handlePowerButtonPress()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        // 1. Acquire WakeLock (Keeps CPU running)
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EmergencyResponder:CrashMonitorWakelock")
        wakeLock?.acquire()

        // 2. Initialize Components
        sensorProvider = AndroidSensorProvider(this)
        notifier = AndroidAlertNotifier(this)
        voiceManager = VoiceAlertManager(this)

        // 3. Setup ML Engine
        val savedSens = SPreferenceManager.getSensitivity()
        val sensitivityEnum = when(savedSens) {
            "HIGH" -> Sensitivity.HIGH
            "MEDIUM" -> Sensitivity.MEDIUM
            else -> Sensitivity.LOW
        }
        engine = CrashDetectionEngine(
            mlAnalyzer = TFLiteCrashMlAnalyzer(this),
            useCase = CrashDetectionUseCase(sensitivityEnum)
        )

        // 4. Register Power Button Receiver (Dynamic Registration is required for SCREEN events)
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenReceiver, filter)

        // 5. Start Service
        startCrashMonitoring()
        startForegroundInternal()
    }

    // --- MANUAL POWER BUTTON LOGIC ---
    private fun handlePowerButtonPress() {
        val now = System.currentTimeMillis()

        // Reset count if presses are too slow (more than 1 second apart)
        if (now - lastPressTime > 1000) {
            powerPressCount = 0
        }

        powerPressCount++
        lastPressTime = now

        Log.d("PowerMonitor", "Power Button Pressed: $powerPressCount times")

        // Trigger on 3 presses (Standard Panic Trigger)
        if (powerPressCount == 3) {
            triggerManualSOS()
            powerPressCount = 0 // Reset to avoid double triggering
        }
    }

    private fun triggerManualSOS() {
        Log.d("CrashService", "🔥 MANUAL SOS TRIGGERED VIA POWER BUTTON")

        // 1. Speak Alert
        voiceManager.speak("Emergency shortcut activated.")

        // 2. Show SOS Notification (Text: "SOS Activated")
        notifier.notifyManualSOS()

        // 3. Start Countdown & Send SMS/GreenAPI
        voiceManager.startCrashCountdown {
            Log.d("SOS", "Manual SOS Countdown finished → Sending Messages")
            SOSUtils.sendSOSViaSMS(this@CrashDetectionService, EMERGENCY_NUMBER)
            SOSUtils.sendSOSViaGreenApi(this@CrashDetectionService, EMERGENCY_NUMBER)
        }
    }

    // --- AUTOMATIC SENSOR LOGIC ---
    private fun startCrashMonitoring() {
        sensorProvider.start { state ->
            val features = featureExtractor.extract(state) ?: return@start
            val result = engine.evaluate(state, features)

            when (result) {
                DetectionResult.Crash -> {
                    if (crashAlreadyDetected) return@start

                    crashAlreadyDetected = true
                    Log.d("CrashService", "🚗 Crash detected! Locking system.")

                    // 1. Show Crash Notification
                    notifier.notifyCrash()

                    // 2. Start Countdown
                    voiceManager.startCrashCountdown {
                        SOSUtils.sendSOSViaSMS(this@CrashDetectionService, EMERGENCY_NUMBER)
                        SOSUtils.sendSOSViaGreenApi(this@CrashDetectionService, EMERGENCY_NUMBER)
                    }

                    // 3. Reset Lock after 15 seconds
                    Handler(Looper.getMainLooper()).postDelayed({
                        crashAlreadyDetected = false
                        Log.d("CrashService", "System unlocked for new crashes.")
                    }, 15000)
                }

                DetectionResult.Snatch -> {
                    if (snatchAlreadyDetected) return@start
                    snatchAlreadyDetected = true

                    // 1. Speak & Notify
                    voiceManager.speak("Phone snatching detected!")
                    notifier.notifySnatch()

                    // 2. Reset Lock after 5 seconds (Shorter delay for snatch)
                    Handler(Looper.getMainLooper()).postDelayed({
                        snatchAlreadyDetected = false
                    }, 5000)
                }

                DetectionResult.None -> Unit
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Unregister receiver to prevent crashes/leaks
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            Log.e("CrashService", "Receiver not registered: ${e.message}")
        }

        sensorProvider.stop()
        voiceManager.shutdown()

        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        Log.d("CrashService", "Service destroyed")
    }

    override fun onBind(intent: Intent?) = null

    // Start Foreground Notification (Required for Service to run in background)
    private fun startForegroundInternal() {
        val channelId = "safety_foreground"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Emergency Monitoring",
                android.app.NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.example.emergencyresponder.R.drawable.app_logo)
            .setContentTitle("Emergency Monitor Active")
            .setContentText("Monitoring Sensors & Power Button")
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }
}