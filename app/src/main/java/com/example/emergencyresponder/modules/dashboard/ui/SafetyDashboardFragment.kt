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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.emergencyresponder.R
import com.example.emergencyresponder.databinding.FragmentSafetyDashboardBinding
import com.example.emergencyresponder.modules.dashboard.data.model.NearbyService
import com.example.emergencyresponder.modules.dashboard.domain.adapters.NearbyServicesAdapter
import com.example.emergencyresponder.modules.dashboard.domain.viewmodel.SafetyDashboardViewModel
import com.example.emergencyresponder.modules.dashboard.ui.service.CrashDetectionService
import com.example.emergencyresponder.modules.dashboard.ui.service.MicListenService

class SafetyDashboardFragment : Fragment() {

    private var _binding: FragmentSafetyDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SafetyDashboardViewModel by viewModels()

    private val REQUEST_NOTIFICATION_PERMISSION = 100
    private val REQUEST_MIC_PERMISSION = 101




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSafetyDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startCrashDetectionService()
        ensureNotificationPermission()

        binding.btnEnableMic.setOnClickListener {
            requestMicPermissions()
            Toast.makeText(requireContext(),"Mic permission granted",Toast.LENGTH_SHORT).show()
        }

        //setupEmergencyContacts()
        setupNearbyServices()
    }
    private fun requestMicPermissions() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_MICROPHONE)
        }

        requestPermissions(permissions.toTypedArray(), REQUEST_MIC_PERMISSION)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 101 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(requireContext(), MicListenService::class.java)
            ContextCompat.startForegroundService(requireContext(), intent)
        }
    }

    private fun setupNearbyServices() {
        binding.nearbyServices.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.nearbyServices.adapter =
            NearbyServicesAdapter(getSampleNearbyServices())
    }

    private fun getSampleNearbyServices(): List<NearbyService> {
        return listOf(
            NearbyService(R.drawable.aid, "Ambulance", "Ghori Town", "15km") {},
            NearbyService(R.drawable.aid, "Police Station", "Ghori Town", "15km") {}
        )
    }

    private fun startCrashDetectionService() {
        val intent = Intent(requireContext(), CrashDetectionService::class.java)
        ContextCompat.startForegroundService(requireContext(), intent)
    }

//    private fun startMicService() {
//        val intent = Intent(requireContext(), MicListenService::class.java)
//        ContextCompat.startForegroundService(requireContext(), intent)
//        Toast.makeText(requireContext(), "Trying to start service...", Toast.LENGTH_SHORT).show()
//    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
