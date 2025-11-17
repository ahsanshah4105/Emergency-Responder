package com.example.emergencyresponder.modules.dashboard.ui
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.emergencyresponder.databinding.ActivitySafetyDashboardBinding

class SafetyDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySafetyDashboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySafetyDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}