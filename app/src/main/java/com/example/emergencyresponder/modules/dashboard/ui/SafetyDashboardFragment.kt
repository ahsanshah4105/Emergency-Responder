package com.example.emergencyresponder.modules.dashboard.ui


import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.navigation.fragment.findNavController
import com.example.emergencyresponder.R
import com.example.emergencyresponder.core.objects.SPreferenceManager
import com.example.emergencyresponder.core.utils.SOSUtils
import com.example.emergencyresponder.databinding.FragmentSafetyDashboardBinding
import com.example.emergencyresponder.modules.auth.data.model.EmergencyContact
import com.example.emergencyresponder.modules.dashboard.data.model.DashboardStatus
import com.example.emergencyresponder.modules.dashboard.domain.viewmodel.SafetyDashboardViewModel
import com.example.emergencyresponder.modules.dashboard.ui.service.CrashDetectionService
import com.example.emergencyresponder.modules.dashboard.ui.service.MicListenService
import com.example.emergencyresponder.modules.dashboard.ui.service.PowerPressAccessibilityService
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
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
        val userName = SPreferenceManager.getUserName()
        binding.txtWelcome.text = "Welcome, ${userName ?: "User"}"

        binding.itemSnatch.setOnClickListener {
            enableSnatchGuard()
        }


        binding.sendAlert.setOnTouchListener { v, event ->

            sendSoS(event, v)

            true
        }




        binding.capsuleSystem.setOnClickListener { ensureNotificationPermission() }

        return binding.root
    }

    private fun sendSoS(event: MotionEvent?, v: View?) {
        when (event?.action) {

            MotionEvent.ACTION_DOWN -> {

                // 🔥 Scale button slightly bigger
                v?.animate()
                    ?.scaleX(1.1f)
                    ?.scaleY(1.1f)
                    ?.setDuration(200)
                    ?.start()

                // Start 3-second hold timer
                sosRunnable = Runnable {

                    // Provide immediate feedback
                    Toast.makeText(requireContext(), "Opening WhatsApp...", Toast.LENGTH_SHORT).show()

                    // ✅ CALL THE NEW FUNCTION
                    SOSUtils.sendSOSOnWhatsApp(requireContext())
                }

                v?.postDelayed(sosRunnable!!, 3000) // ⏳ 3 seconds hold
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {

                // Reset button size
                v?.animate()
                    ?.scaleX(1f)
                    ?.scaleY(1f)
                    ?.setDuration(200)
                    ?.start()

                // Cancel if released early
                sosRunnable?.let { v?.removeCallbacks(it) }
            }
        }
    }
    private fun enableSnatchGuard() {
        if (!isAccessibilityServiceEnabled()) {

            // 1. Create the dialog but don't show it yet (or capture the result of show())
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Enable Snatch Guard")
                .setMessage(
                    "To detect phone snatching, Emergency Responder needs Accessibility Permission.\n\n" +
                            "Just tap Enable on the next screen."
                )
                .setPositiveButton("Enable Now") { _, _ ->
                    openDirectAccessibilitySettings()
                }
                .setNegativeButton("Cancel", null)
                .show() // Shows the dialog and returns the instance

            // 2. Get the button from the dialog instance and set the color
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                ContextCompat.getColor(requireContext(), R.color.primaryColor)
            )

            // Optional: Set negative button color too
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                ContextCompat.getColor(requireContext(), R.color.black)
            )

        } else {
            Toast.makeText(requireContext(), "Snatch Guard is Active ✅", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startCrashDetectionService()
        ensureNotificationPermission()
        updateSystemStatus()


        startMicServiceIfPermissionGranted() // ✅ ADD THIS

        lifecycleScope.launch {
            viewModel.dashboardStatus.collectLatest { status ->
                updateIndicators(status)
            }
        }
        viewModel.checkEmergencyContactsExist()

        viewModel.navigateToEmergencyContacts.observe(viewLifecycleOwner) { shouldNavigate ->
            Log.d("SafetyFragment", "Observer triggered. Should Navigate: $shouldNavigate") // <--- ADD LOG

            if (shouldNavigate) {
                showEmptyContactsWarning()
                viewModel.onNavigationHandled()
            }
        }
    }
    private fun showEmptyContactsWarning() {
        Snackbar.make(binding.root, "⚠️ No Emergency Contacts Found!", Snackbar.LENGTH_INDEFINITE)
            .setAction("ADD NOW") {
                navigateToEmergencyContactFragment()
            }
            .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.primaryColor)) // Use a red/alert color
            .show()

    }

    private fun navigateToEmergencyContactFragment() {
        try {
            findNavController().navigate(R.id.action_safetyDashboardFragment_to_emergencyContactFragment2)
        } catch (e: Exception) {
            // Fallback if action ID is different, try global ID
            try {
                findNavController().navigate(R.id.emergencyContactFragment)
            } catch (e2: Exception) {
                Log.e("Navigation", "Could not navigate: ${e2.message}")
            }
        }
    }

    private fun startMicServiceIfPermissionGranted() {

        val granted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            val intent = Intent(requireContext(), MicListenService::class.java)
            ContextCompat.startForegroundService(requireContext(), intent)

            Log.d("SafetyDashboard", "✅ MicListenService started automatically")
        } else {
            Log.d("SafetyDashboard", "❌ Mic permission not granted yet")
        }
    }

    private fun requestMicPermission() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) permissions.add(Manifest.permission.FOREGROUND_SERVICE_MICROPHONE)
        micPermissionRequest.launch(permissions.toTypedArray())
    }

    private fun openDirectAccessibilitySettings() {
        val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)

        Toast.makeText(
            requireContext(),
            "Scroll down and enable Emergency Responder",
            Toast.LENGTH_LONG
        ).show()
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
        updateSystemStatus()
        viewModel.checkEmergencyContactsExist()

        checkSensitivityTrigger()
    }

    private fun checkSensitivityTrigger() {
        // If user cancelled 3 or more times, ask them to adjust
        if (SPreferenceManager.getCancelCount() >= 3) {
            showSensitivityDialog()
        }
    }

    private fun showSensitivityDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_sensitivity_settings, null)

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false) // Force user to choose or click Not Now

        val dialog = builder.create()


        // Bind Views
        val cardHigh = dialogView.findViewById<MaterialCardView>(R.id.cardHigh)
        val cardMed = dialogView.findViewById<MaterialCardView>(R.id.cardMedium)
        val cardLow = dialogView.findViewById<MaterialCardView>(R.id.cardLow)
        val btnSave = dialogView.findViewById<View>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancel)

        // Initial Selection State (Get current)
        var selectedLevel = SPreferenceManager.getSensitivity()

        // Helper to update UI
        fun updateSelectionUI(level: String) {
            val activeColor = ContextCompat.getColor(requireContext(), R.color.primaryColor) // Your app color
            val inactiveColor = Color.parseColor("#E0E0E0")

            // Reset all
            cardHigh.strokeColor = inactiveColor
            cardMed.strokeColor = inactiveColor
            cardLow.strokeColor = inactiveColor

            // Highlight selected
            when(level) {
                "HIGH" -> cardHigh.strokeColor = activeColor
                "MEDIUM" -> cardMed.strokeColor = activeColor
                "LOW" -> cardLow.strokeColor = activeColor
            }
        }

        updateSelectionUI(selectedLevel)

        // Click Listeners
        cardHigh.setOnClickListener { selectedLevel = "HIGH"; updateSelectionUI("HIGH") }
        cardMed.setOnClickListener { selectedLevel = "MEDIUM"; updateSelectionUI("MEDIUM") }
        cardLow.setOnClickListener { selectedLevel = "LOW"; updateSelectionUI("LOW") }

        btnSave.setOnClickListener {
            // 1. Save new sensitivity
            SPreferenceManager.setSensitivity(selectedLevel)

            // 2. Reset the counter to 0 so it doesn't show again immediately
            SPreferenceManager.resetCancelCount()

            // 3. Restart Service to apply changes
            val intent = Intent(requireContext(), CrashDetectionService::class.java)
            requireContext().stopService(intent)
            ContextCompat.startForegroundService(requireContext(), intent)

            Toast.makeText(requireContext(), "Sensitivity updated to $selectedLevel", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            // Just reset counter so it doesn't bug them immediately again
            SPreferenceManager.resetCancelCount()
            dialog.dismiss()
        }

        dialog.show()
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
                    SOSUtils.sendSOSOnWhatsApp(requireContext())
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
