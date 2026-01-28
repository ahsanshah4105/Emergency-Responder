package com.example.emergencyresponder.modules.dashboard.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.emergencyresponder.R
import com.example.emergencyresponder.databinding.FragmentSafetyDashboardBinding
import com.example.emergencyresponder.modules.dashboard.domain.viewmodel.SafetyDashboardViewModel
import com.example.emergencyresponder.modules.dashboard.ui.service.CrashDetectionService
import com.example.emergencyresponder.modules.dashboard.ui.service.MicListenService
import com.example.emergencyresponder.modules.dashboard.ui.service.PowerPressAccessibilityService

class SafetyDashboardFragment : Fragment() {

    private var _binding: FragmentSafetyDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SafetyDashboardViewModel by viewModels()

    private val REQUEST_NOTIFICATION_PERMISSION = 100
    private val REQUEST_MIC_PERMISSION = 101


    private val micPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val granted =
                result[Manifest.permission.RECORD_AUDIO] == true &&
                        (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                                result[Manifest.permission.FOREGROUND_SERVICE_MICROPHONE] == true)

            if (granted) {
                val intent = Intent(requireContext(), MicListenService::class.java)
                ContextCompat.startForegroundService(requireContext(), intent)
                Toast.makeText(requireContext(), "Mic permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Mic permission required", Toast.LENGTH_SHORT).show()
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSafetyDashboardBinding.inflate(inflater, container, false)

        binding.itemSnatch.setOnClickListener {
            if (!isAccessibilityServiceEnabled()) {
                Toast.makeText(requireContext(), "Please enable Emergency Responder in Accessibility Settings", Toast.LENGTH_LONG).show()
                startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } else {
                Toast.makeText(requireContext(), "Snatch Guard is Active", Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startCrashDetectionService()
        ensureNotificationPermission()

        // Initial check
        updateIndicators()

        binding.itemAudio.setOnClickListener {
            val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions.add(Manifest.permission.FOREGROUND_SERVICE_MICROPHONE)
            }
            micPermissionRequest.launch(permissions.toTypedArray())
        }

        binding.capsuleSystem.setOnClickListener { ensureNotificationPermission() }
    }

    // Call this whenever you want to refresh the UI (e.g., in onResume)
    private fun updateIndicators() {
        // --- 1. Audio Check ---
        val hasMic = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        binding.dotAudio.setBackgroundResource(if (hasMic) R.drawable.circle_indicator_green else R.drawable.circle_indicator_red)

        // --- 2. Crash Check (Critical for System Status) ---
        val hasLocation = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasNotif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true

        binding.dotCrash.setBackgroundResource(if (hasLocation && hasNotif) R.drawable.circle_indicator_green else R.drawable.circle_indicator_red)

        // --- 3. Snatch Check ---
        val hasAccessibility = isAccessibilityServiceEnabled()
        binding.dotSnatch.setBackgroundResource(if (hasAccessibility) R.drawable.circle_indicator_green else R.drawable.circle_indicator_red)

        // --- 4. TOP CAPSULE SYSTEM STATUS ---
        // If Notifications or Location are missing, the system is NOT fully active
        if (hasNotif && hasLocation) {
            // Active State
            binding.capsuleSystem.setBackgroundResource(R.drawable.bg_capsule_green)
            binding.capsuleSystem.findViewById<View>(R.id.indicatorDot).setBackgroundResource(R.drawable.circle_indicator_green)
            binding.capsuleSystem.findViewById<android.widget.TextView>(R.id.tvSystemStatus).apply {
                text = "SYSTEM ACTIVE"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_green_text))
            }
        } else {
            // Inactive/Warning State
            binding.capsuleSystem.setBackgroundResource(R.drawable.bg_capsule_red) // You need to create this drawable
            binding.capsuleSystem.findViewById<View>(R.id.indicatorDot).setBackgroundResource(R.drawable.circle_indicator_red)
            binding.capsuleSystem.findViewById<android.widget.TextView>(R.id.tvSystemStatus).apply {
                text = "NOT ACTIVE"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.primaryColor))
            }
        }
    }
    // Helper to check if your Accessibility Service is actually running
    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName = android.content.ComponentName(requireContext(), PowerPressAccessibilityService::class.java)
        val enabledServices = android.provider.Settings.Secure.getString(requireContext().contentResolver, android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return enabledServices?.contains(expectedComponentName.flattenToString()) == true
    }

    override fun onResume() {
        super.onResume()
        updateIndicators() // Refresh when user returns from settings
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            // IMPORTANT: Update UI immediately after user interacts with dialog
            updateIndicators()

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCrashDetectionService()
            }
        }
    }

    private fun startCrashDetectionService() {
        val intent = Intent(requireContext(), CrashDetectionService::class.java)
        ContextCompat.startForegroundService(requireContext(), intent)
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            )

            if (status == PackageManager.PERMISSION_DENIED) {
                // Check if user previously denied permanently
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    // User denied once, show the system dialog again
                    requestPermissions(
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        REQUEST_NOTIFICATION_PERMISSION
                    )
                } else {
                    // Either first time OR permanently denied
                    // We try to request, but if the dialog doesn't show, we send to settings
                    val intent = Intent().apply {
                        action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                    }

                    Toast.makeText(requireContext(), "Please enable notifications for full protection", Toast.LENGTH_LONG).show()
                    startActivity(intent)
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
