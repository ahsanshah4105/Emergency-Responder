package com.example.emergencyresponder.modules.dashboard.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.model.EmergencyContact
import com.example.emergencyresponder.modules.dashboard.data.model.DashboardStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SafetyDashboardViewModel : ViewModel() {

    private val _dashboardStatus = MutableStateFlow(DashboardStatus())
    val dashboardStatus = _dashboardStatus.asStateFlow()
    val uid = FirebaseAuth.getInstance().currentUser?.uid


    private val _navigateToEmergencyContacts = MutableLiveData<Boolean>()
    val navigateToEmergencyContacts: LiveData<Boolean> = _navigateToEmergencyContacts

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

    fun checkEmergencyContactsExist() {
        viewModelScope.launch {
            try {

                if (uid.isNullOrEmpty()) {
                    return@launch
                }
                val userRemoteDataSource = UserRemoteDataSource()
                val user = userRemoteDataSource.getUser(uid)

                val hasValidContact = user.emergencyContacts.any { it.phone.isNotBlank() }
                if (!hasValidContact) {
                    _navigateToEmergencyContacts.value = true
                } else {
                    Log.d("SafetyVM", "✅ User has valid contacts")
                }

            } catch (e: Exception) {
                Log.e("SafetyVM", "❌ Error checking contacts", e)
            }
        }
    }
    fun onNavigationHandled() {
        _navigateToEmergencyContacts.value = false
    }

}
