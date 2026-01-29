package com.example.emergencyresponder.modules.timestamp.ui


import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.emergencyresponder.core.utils.SOSUtils
import com.example.emergencyresponder.databinding.ActivityTimeStampBinding
import com.example.emergencyresponder.modules.dashboard.ui.service.CrashDetectionService
import com.example.emergencyresponder.modules.timestamp.domain.viewmodel.TimeStampViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class TimeStampActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimeStampBinding
    private  var db = Firebase.firestore
    private val viewModel: TimeStampViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimeStampBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1️⃣ Display the current remaining seconds immediately
        viewModel.updateCountdown(CrashCountdownManager.remainingSeconds.toInt())

        // 2️⃣ Subscribe to future updates from CrashCountdownManager
        CrashCountdownManager.addListener(
            onTick = { sec ->
                runOnUiThread {
                    viewModel.updateCountdown(sec.toInt()) // updates UI
                }
            },
            onFinish = {
                runOnUiThread {
                    viewModel.updateCountdown(0)
                }
            }
        )

        // 3️⃣ Observe ViewModel to update actual UI
        viewModel.secondRemaining.observe(this) { seconds ->
            binding.countDownTimer.text = "$seconds s"
        }

        viewModel.progress.observe(this) { progress ->
            binding.circularProgressBar.progress = progress
        }

        binding.iAmOkay.setOnClickListener {
            // ✅ Cancel global countdown and any pending alert
            CrashCountdownManager.cancel()

            // ✅ Stop voice
            (application as? CrashDetectionService)?.voiceManager?.shutdown()

            // ✅ Optionally speak safe message
            (application as? CrashDetectionService)?.voiceManager?.speak("You are safe. Countdown cancelled.")

            finish()
        }



        binding.sendAlert.setOnClickListener {
            SOSUtils.sendSOSOnWhatsApp(this, "+923068988678")
        }
    }

}
