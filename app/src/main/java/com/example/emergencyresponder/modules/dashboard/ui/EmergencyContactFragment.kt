package com.example.emergencyresponder.modules.dashboard.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.emergencyresponder.R
import com.example.emergencyresponder.databinding.FragmentEmergencyContactBinding
import com.example.emergencyresponder.databinding.FragmentSafetyDashboardBinding
import com.example.emergencyresponder.modules.dashboard.data.model.EmergencyContacts
import com.example.emergencyresponder.modules.dashboard.domain.adapters.EmergencyContactsAdapter

class EmergencyContactFragment : Fragment() {
    private var _binding: FragmentEmergencyContactBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEmergencyContactBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupEmergencyContacts()
    }

    private fun setupEmergencyContacts() {
        val contacts = listOf(
            EmergencyContacts(R.drawable.aid, "Ambulance Service", "0311-1234567") {},
            EmergencyContacts(R.drawable.aid, "Police", "0312-7654321") {},
                    EmergencyContacts(R.drawable.aid, "Police", "0312-7654321") {}
        )

        _binding?.emergencyContactsList?.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        _binding?.emergencyContactsList?.adapter = EmergencyContactsAdapter(contacts)
    }
}