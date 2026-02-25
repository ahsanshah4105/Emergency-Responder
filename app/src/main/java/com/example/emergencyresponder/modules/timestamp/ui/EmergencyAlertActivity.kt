package com.example.emergencyresponder.modules.timestamp.ui


import CrashCountdownManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.emergencyresponder.core.base.EmergencyResponderApp
import com.example.emergencyresponder.core.utils.SOSBlastManager
import com.example.emergencyresponder.databinding.ActivityTimeStampBinding
import com.example.emergencyresponder.modules.dashboard.ui.service.CrashDetectionService
import com.example.emergencyresponder.modules.timestamp.ui.viewmodel.EmergencyAlertViewModel
import com.example.emergencyresponder.modules.timestamp.ui.viewmodelfactory.TimeStampViewModelFactory

class EmergencyAlertActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimeStampBinding
    private val viewModel: EmergencyAlertViewModel by viewModels {
        val container = (application as EmergencyResponderApp).appContainer

        TimeStampViewModelFactory(
            countdownManager = CrashCountdownManager,
            crashRepository = container.crashRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimeStampBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.secondsRemaining.observe(this) { seconds ->
            binding.countDownTimer.text = "${seconds}s"
        }

        viewModel.progress.observe(this) { progressValue ->
            binding.circularProgressBar.progress = progressValue
        }

        viewModel.finishActivity.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                finish()
            }
        }
    }

    private fun setupClickListeners() {
        binding.iAmOkay.setOnClickListener {
            viewModel.onUserIsOkay()

            val intent = Intent(this, CrashDetectionService::class.java).apply {
                action = "ACTION_CANCEL_EMERGENCY"
            }
            startService(intent)

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