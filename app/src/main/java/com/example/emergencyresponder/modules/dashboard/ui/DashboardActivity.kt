package com.example.emergencyresponder.modules.dashboard.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.emergencyresponder.R
import com.example.emergencyresponder.databinding.ActivityDashboardBinding
import com.example.emergencyresponder.modules.dashboard.ui.service.CrashDetectionService
import com.example.emergencyresponder.modules.dashboard.ui.service.MicListenService
import com.google.android.material.bottomnavigation.BottomNavigationView
class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val REQUEST_NOTIFICATION_PERMISSION = 100
    private val micPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted =
                permissions[Manifest.permission.RECORD_AUDIO] == true &&
                        (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                                permissions[Manifest.permission.FOREGROUND_SERVICE_MICROPHONE] == true)

            if (granted) {
              //  startMicService()
            } else {
                Toast.makeText(this, "Mic permission required", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = Intent(this, MicListenService::class.java)
        ContextCompat.startForegroundService(this, intent)

        startCrashDetectionService()
        ensureNotificationPermission()


            val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions.add(Manifest.permission.FOREGROUND_SERVICE_MICROPHONE)
            }
            micPermissionRequest.launch(permissions.toTypedArray())

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setupWithNavController(navController)
    }

    private fun startCrashDetectionService() {
        val intent = Intent(this, CrashDetectionService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

//    fun startMicService() {
//        val intent = Intent(this, MicListenService::class.java)
//        ContextCompat.startForegroundService(this, intent)
//        Toast.makeText(this, "Mic service started", Toast.LENGTH_SHORT).show()
//    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }
}