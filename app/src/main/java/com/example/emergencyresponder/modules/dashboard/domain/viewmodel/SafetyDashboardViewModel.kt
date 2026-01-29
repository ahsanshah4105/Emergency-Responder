package com.example.emergencyresponder.modules.dashboard.domain.viewmodel

import androidx.lifecycle.ViewModel
import com.example.emergencyresponder.modules.auth.data.model.EmergencyContact
import com.example.emergencyresponder.modules.dashboard.data.model.DashboardStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SafetyDashboardViewModel : ViewModel() {

    private val _dashboardStatus = MutableStateFlow(DashboardStatus())
    val dashboardStatus = _dashboardStatus.asStateFlow()
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    fun updateStatus(
        hasMic: Boolean,
        hasLocation: Boolean,
        hasNotif: Boolean,
        hasAccessibility: Boolean
    ) {
        val crash = hasLocation && hasNotif
        _dashboardStatus.value = DashboardStatus(audio = hasMic, crash = crash, snatch = hasAccessibility)
    }

    fun updateAudioStatus(hasMic: Boolean) {
        _dashboardStatus.value = _dashboardStatus.value.copy(audio = hasMic)
    }

    fun updateSnatchStatus(hasAccessibility: Boolean) {
        _dashboardStatus.value = _dashboardStatus.value.copy(snatch = hasAccessibility)
    }

    fun fetchEmergencyContacts(
        onResult: (List<EmergencyContact>) -> Unit,
        onError: () -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {

                    val contacts = document.get("emergencyContacts") as? List<Map<String, Any>>

                    val contactList = contacts?.map {
                        EmergencyContact(
                            name = it["name"].toString(),
                            phone = it["phone"].toString()
                        )
                    } ?: emptyList()

                    onResult(contactList)
                }
            }
            .addOnFailureListener {
                onError()
            }
    }


}
