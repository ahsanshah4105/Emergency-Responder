package com.example.emergencyresponder.modules.dashboard.ui.service

import CrashCountdownManager
import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import android.util.Log
import com.example.emergencyresponder.core.base.EmergencyResponderApp
import com.example.emergencyresponder.core.utils.SOSBlastManager
import com.example.emergencyresponder.modules.dashboard.data.model.DetectionResult
import com.example.emergencyresponder.modules.dashboard.domain.engine.CrashDetectionEngine
import com.example.emergencyresponder.modules.dashboard.domain.engine.FeatureExtractor
import com.example.emergencyresponder.modules.dashboard.domain.engine.PowerButtonMonitor
import com.example.emergencyresponder.modules.dashboard.domain.notifier.AlertNotifier
import com.example.emergencyresponder.modules.dashboard.domain.notifier.VoiceAlertManager
import com.example.emergencyresponder.modules.dashboard.domain.repository.SensorProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import throttleFirst

class CrashDetectionService : Service() {

    private lateinit var sensorProvider: SensorProvider
    private lateinit var engine: CrashDetectionEngine
    private lateinit var notifier: AlertNotifier
    private lateinit var powerMonitor: PowerButtonMonitor
    lateinit var voiceAlertManager: VoiceAlertManager
    private val featureExtractor = FeatureExtractor()
    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val ACTION_CANCEL = "ACTION_CANCEL_EMERGENCY"


    private val cancelReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_CANCEL) {
                stopEmergencySequence()
            }
        }
    }

    companion object {
        const val ACTION_TRIGGER_SOS = "com.example.emergencyresponder.ACTION_TRIGGER_SOS"
    }


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        startForegroundInternal()

        super.onCreate()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SafetyApp:Monitor")
        wakeLock?.acquire(10 * 60 * 1000L)

        val appContainer = (application as EmergencyResponderApp).appContainer
        sensorProvider = appContainer.sensorProvider
        engine = appContainer.getCrashEngine()
        notifier = appContainer.notifier
        voiceAlertManager = appContainer.voiceAlertManager
        powerMonitor = PowerButtonMonitor(this)

        registerSystemReceivers()

        startMonitoringPipeline()
        startPowerButtonPipeline()

    }

    private fun startPowerButtonPipeline() {
        serviceScope.launch {
            powerMonitor.observeTriplePress()
                .collect {
                    withContext(Dispatchers.Main) { triggerManualSOS() }
                }
        }
    }
    private fun registerSystemReceivers() {
        val cancelFilter = IntentFilter(ACTION_CANCEL)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(cancelReceiver, cancelFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(cancelReceiver, cancelFilter)
        }
    }


    private fun startMonitoringPipeline() {
        serviceScope.launch(Dispatchers.Default) {
            sensorProvider.getSensorUpdates()
                .mapNotNull { state ->
                    val features = featureExtractor.extract(state)
                    features?.let {
                        val result = engine.evaluate(state, it)
                        // Sirf tab result bhejen jab wo None NA HO
                        if (result !is DetectionResult.None) {
                            Log.d("CrashService", "🚀 REAL CRASH DETECTED: $result")
                            result
                        } else {
                            null
                        }
                    }
                }
                .throttleFirst(3000L)
                .collect { result ->
                    withContext(Dispatchers.Main) {
                        Log.d("CrashService", "✅ Collecting Final Result for UI: $result")
                        handleDetectionResult(result)
                    }
                }
        }
    }
    private fun handleDetectionResult(result: DetectionResult) {

        Log.d("CrashService", "Handling: ${result::class.simpleName}")

        when (result) {
            is DetectionResult.Crash -> {
                Log.d("CrashService", "Triggering Crash Notification UI")
                notifier.notifyCrash()
                triggerEmergencySequence()
            }
            is DetectionResult.Snatch -> {
                Log.d("CrashService", "Triggering Snatch Notification UI")
                notifier.notifySnatch()
                triggerEmergencySequence()
            }
            else -> {
                Log.d("CrashService", "Ignored Result: $result")
            }
        }
    }
    private fun triggerEmergencySequence() {
        voiceAlertManager.startVoiceMonitoring(serviceScope) {
            SOSBlastManager.sendBlastToAllUsers(this@CrashDetectionService)
        }
    }
    private fun triggerManualSOS() {
        notifier.notifyManualSOS()
        triggerEmergencySequence()
    }
    private fun stopEmergencySequence() {
        CrashCountdownManager.cancel()
        voiceAlertManager.speak("You are safe. Countdown cancelled.")
        notifier.cancel()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == ACTION_TRIGGER_SOS) {
            startForegroundInternal()
            SOSBlastManager.sendBlastToAllUsers(this)
            return START_STICKY
        }

        val triggeredByPower = intent?.getBooleanExtra("TRIPLE_POWER_PRESS", false) ?: false
        if (triggeredByPower) {
            triggerManualSOS()
            return START_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(cancelReceiver) } catch (e: Exception) { /* ... */ }

        voiceAlertManager.shutdown()
        serviceScope.cancel()

        if (wakeLock?.isHeld == true) wakeLock?.release()
    }
    override fun onBind(intent: Intent?) = null

    private fun startForegroundInternal() {
        val channelId = "safety_foreground"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Emergency Monitoring",
                android.app.NotificationManager.IMPORTANCE_HIGH
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