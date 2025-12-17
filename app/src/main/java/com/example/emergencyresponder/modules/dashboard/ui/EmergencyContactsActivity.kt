package com.example.emergencyresponder.modules.dashboard.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.emergencyresponder.R
import com.example.emergencyresponder.databinding.ActivityEmergencyContactsBinding
import com.example.emergencyresponder.databinding.ActivitySafetyDashboardBinding
import com.example.emergencyresponder.modules.dashboard.adapters.EmergencyContactsAdapter
import com.example.emergencyresponder.modules.dashboard.data.EmergencyContacts

class EmergencyContactsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmergencyContactsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEmergencyContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        binding.allEmergencyContacts.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val contactsList = EmergencyContactsAdapter(contacts)
        binding.allEmergencyContacts.adapter = contactsList
    }
}