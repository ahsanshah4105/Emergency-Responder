package com.example.emergencyresponder.modules.dashboard.ui

import android.content.Intent
import android.os.Build
import com.example.emergencyresponder.R
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.emergencyresponder.databinding.ActivitySafetyDashboardBinding
import com.example.emergencyresponder.modules.dashboard.adapters.EmergencyContactsAdapter
import com.example.emergencyresponder.modules.dashboard.adapters.NearbyServicesAdapter
import com.example.emergencyresponder.modules.dashboard.data.EmergencyContacts
import com.example.emergencyresponder.modules.dashboard.data.NearbyService
import com.example.emergencyresponder.modules.dashboard.ui.service.CrashDetectionService
import com.example.emergencyresponder.modules.dashboard.domain.useCase.viewmodel.SafetyDashboardViewModel
import androidx.activity.viewModels


class SafetyDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySafetyDashboardBinding
    private val viewModel: SafetyDashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySafetyDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        startCrashDetectionService()
        val contacts = listOf(
            EmergencyContacts(
                iconRes = R.drawable.aid, // replace with your drawable
                name = "Ambulance Service",
                phone = "0311-1234567",
                sosAction = { /* handle SOS call */ }
            ),
            EmergencyContacts(
                iconRes = R.drawable.aid, // replace with your drawable
                name = "Police",
                phone = "0312-7654321",
                sosAction = { /* handle SOS call */ }
            )
        )


        binding.contactsList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val contactsList = EmergencyContactsAdapter(contacts)
        binding.contactsList.adapter = contactsList

        binding.nearbyServices.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val adapter = NearbyServicesAdapter(getSampleNearbyServices())
        binding.nearbyServices.adapter = adapter

    }


    private fun startCrashDetectionService() {
        val intent = Intent(this, CrashDetectionService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "Emergency detection started", Toast.LENGTH_SHORT).show()
    }


    private fun getSampleNearbyServices(): List<NearbyService> {
        return listOf(
            NearbyService(R.drawable.aid, "Ambulance", "Ghori Town", "15km") {
                Toast.makeText(this, "Ambulance SOS triggered", Toast.LENGTH_SHORT)
                    .show()
            },
            NearbyService(R.drawable.aid, "Police Station", "Ghori Town", "15km") {
                Toast.makeText(this, "Police SOS triggered", Toast.LENGTH_SHORT).show()
            },
            NearbyService(R.drawable.aid, "Fire Brigade", "I-8 Sector", "10km") {
                Toast.makeText(this, "Fire Brigade SOS triggered", Toast.LENGTH_SHORT).show()
            },
            NearbyService(R.drawable.aid, "City Hospital", "PWD Road", "5km") {
                Toast.makeText(this, "Hospital SOS triggered", Toast.LENGTH_SHORT).show()
            },
            NearbyService(R.drawable.aid, "Doctor Clinic", "Bahria Town", "9km") {
                Toast.makeText(this, "Doctor SOS triggered", Toast.LENGTH_SHORT).show()
            },
            NearbyService(R.drawable.aid, "Security Office", "F-10 Markaz", "7km") {
                Toast.makeText(this, "Security SOS triggered", Toast.LENGTH_SHORT).show()
            }
        )
    }

}