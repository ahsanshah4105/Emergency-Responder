package com.example.emergencyresponder.modules.timestamp.ui


import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.emergencyresponder.core.utils.SOSUtils
import com.example.emergencyresponder.databinding.ActivityTimeStampBinding
import com.example.emergencyresponder.modules.timestamp.domain.viewmodel.TimeStampViewModel

class TimeStampActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimeStampBinding
    private val viewModel: TimeStampViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimeStampBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.secondsRemaining.observe(this) { seconds ->
            binding.tvProgressPercentage.text = "$seconds s"
            if (seconds == 0) finish()
        }

        viewModel.progress.observe(this) { progress ->
            binding.circularProgressBar.progress = progress
        }

        viewModel.startCountdown(60)

        binding.iAmOkay.setOnClickListener {
            finish()
        }
        binding.sendAlert.setOnClickListener {
            SOSUtils.sendSOSViaSMS(this, "+923068988678")
        }

    }

}
