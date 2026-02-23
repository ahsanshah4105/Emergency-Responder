package com.example.emergencyresponder.modules.timestamp.ui


import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.emergencyresponder.core.manager.SPreferenceManager
import com.example.emergencyresponder.core.utils.SOSBlastManager
import com.example.emergencyresponder.databinding.ActivityTimeStampBinding
import com.example.emergencyresponder.modules.timestamp.ui.viewmodel.TimeStampViewModel

class TimeStampActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimeStampBinding
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
            SPreferenceManager.incrementCancelCount()

            // ✅ FIX: Set the package name to ensure the Service receives it
            val intent = Intent("ACTION_CANCEL_EMERGENCY").apply {
                setPackage(packageName)
            }
            sendBroadcast(intent)

            // Stop local listeners immediately
            CrashCountdownManager.cancel()

            finish()
        }


        binding.sendAlert.setOnClickListener {
            binding.sendAlert.isEnabled = false

            SOSBlastManager.sendBlastToAllUsers(this)

            binding.sendAlert.postDelayed({ binding.sendAlert.isEnabled = true }, 2000)
        }
    }

}