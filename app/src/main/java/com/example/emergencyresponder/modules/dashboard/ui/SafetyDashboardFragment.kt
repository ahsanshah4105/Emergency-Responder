package com.example.emergencyresponder.modules.dashboard.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.emergencyresponder.R
import com.example.emergencyresponder.core.utils.SOSUtils
import com.example.emergencyresponder.databinding.FragmentSafetyDashboardBinding
import com.example.emergencyresponder.modules.auth.data.model.EmergencyContact
import com.example.emergencyresponder.modules.dashboard.data.model.DashboardStatus
import com.example.emergencyresponder.modules.dashboard.domain.viewmodel.SafetyDashboardViewModel
import com.example.emergencyresponder.modules.dashboard.ui.service.CrashDetectionService
import com.example.emergencyresponder.modules.dashboard.ui.service.MicListenService
import com.example.emergencyresponder.modules.dashboard.ui.service.PowerPressAccessibilityService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch




class SafetyDashboardFragment : Fragment() {

    private var _binding: FragmentSafetyDashboardBinding? = null
    private val binding get() = _binding!!
    private var sosRunnable: Runnable? = null

    private val viewModel: SafetyDashboardViewModel by viewModels()
    private val REQUEST_NOTIFICATION_PERMISSION = 100

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
                viewModel.updateAudioStatus(true)
            } else {
                Toast.makeText(requireContext(), "Mic permission required", Toast.LENGTH_SHORT).show()
                viewModel.updateAudioStatus(false)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSafetyDashboardBinding.inflate(inflater, container, false)

        binding.itemAudio.setOnClickListener { requestMicPermission() }

        binding.itemSnatch.setOnClickListener {
            val enabled = isAccessibilityServiceEnabled()
            viewModel.updateSnatchStatus(enabled)
            if (!enabled) {
                Toast.makeText(requireContext(), "Please enable Emergency Responder in Accessibility Settings", Toast.LENGTH_LONG).show()
                startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } else {
                Toast.makeText(requireContext(), "Snatch Guard is Active", Toast.LENGTH_SHORT).show()
            }
        }


        binding.sendAlert.setOnTouchListener { v, event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {

                    // 🔥 Scale button slightly bigger
                    v.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(200)
                        .start()

                    // Start 3-second hold timer
                    sosRunnable = Runnable {

                        Toast.makeText(requireContext(), "Fetching contacts...", Toast.LENGTH_SHORT).show()

                        viewModel.fetchEmergencyContacts(
                            onResult = { contacts ->
                                showContactsDialog(contacts)
                            },
                            onError = {
                                Toast.makeText(requireContext(), "Failed to fetch contacts", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    v.postDelayed(sosRunnable!!, 3000) // ⏳ 3 seconds hold
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {

                    // Reset button size if user releases early
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start()

                    // Cancel SOS trigger if not completed
                    sosRunnable?.let { v.removeCallbacks(it) }
                }
            }

            true
        }




        binding.capsuleSystem.setOnClickListener { ensureNotificationPermission() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startCrashDetectionService()
        ensureNotificationPermission()
        updateSystemStatus()
        lifecycleScope.launch {
            viewModel.dashboardStatus.collectLatest { status ->
                updateIndicators(status)
            }
        }
    }

    private fun requestMicPermission() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) permissions.add(Manifest.permission.FOREGROUND_SERVICE_MICROPHONE)
        micPermissionRequest.launch(permissions.toTypedArray())
    }

    private fun updateSystemStatus() {
        val hasMic = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val hasLocation = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasNotif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
        val hasAccessibility = isAccessibilityServiceEnabled()

        viewModel.updateStatus(
            hasMic = hasMic,
            hasLocation = hasLocation,
            hasNotif = hasNotif,
            hasAccessibility = hasAccessibility
        )
    }

    private fun updateIndicators(status: DashboardStatus) {
        binding.dotAudio.setBackgroundResource(if (status.audio) R.drawable.circle_indicator_green else R.drawable.circle_indicator_red)
        binding.dotCrash.setBackgroundResource(if (status.crash) R.drawable.circle_indicator_green else R.drawable.circle_indicator_red)
        binding.dotSnatch.setBackgroundResource(if (status.snatch) R.drawable.circle_indicator_green else R.drawable.circle_indicator_red)

        // Capsule system status
        if (status.crash) {
            binding.capsuleSystem.setBackgroundResource(R.drawable.bg_capsule_green)
            binding.capsuleSystem.findViewById<View>(R.id.indicatorDot).setBackgroundResource(R.drawable.circle_indicator_green)
            binding.capsuleSystem.findViewById<android.widget.TextView>(R.id.tvSystemStatus).apply {
                text = "SYSTEM ACTIVE"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_green_text))
            }
        } else {
            binding.capsuleSystem.setBackgroundResource(R.drawable.bg_capsule_red)
            binding.capsuleSystem.findViewById<View>(R.id.indicatorDot).setBackgroundResource(R.drawable.circle_indicator_red)
            binding.capsuleSystem.findViewById<android.widget.TextView>(R.id.tvSystemStatus).apply {
                text = "NOT ACTIVE"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.primaryColor))
            }
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName = android.content.ComponentName(requireContext(), PowerPressAccessibilityService::class.java)
        val enabledServices = android.provider.Settings.Secure.getString(requireContext().contentResolver, android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return enabledServices?.contains(expectedComponentName.flattenToString()) == true
    }

    private fun startCrashDetectionService() {
        val intent = Intent(requireContext(), CrashDetectionService::class.java)
        ContextCompat.startForegroundService(requireContext(), intent)
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
            if (status == PackageManager.PERMISSION_DENIED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATION_PERMISSION)
                } else {
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

    override fun onResume() {
        super.onResume()
        updateSystemStatus() // refresh when returning from settings
    }
    private fun showContactsDialog(contacts: List<EmergencyContact>) {

        if (contacts.isEmpty()) {
            Toast.makeText(requireContext(), "No emergency contacts found", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Send SOS Alert")
            .setMessage("Are you sure you want to send SOS to ALL emergency contacts?")
            .setPositiveButton("Yes") { _, _ ->

                contacts.forEach {
                    SOSUtils.sendSOSOnWhatsApp(requireContext(), it.phone)
                }

                Toast.makeText(requireContext(), "Sending SOS to all contacts...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
