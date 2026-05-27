package com.example.emergencyresponder.modules.dashboard.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencyresponder.core.domain.model.EmergencyContact
import com.example.emergencyresponder.core.domain.coroutines.DispatcherProvider
import com.example.emergencyresponder.core.domain.session.AuthSession
import com.example.emergencyresponder.modules.dashboard.domain.model.DashboardStatus
import com.example.emergencyresponder.modules.dashboard.domain.usecase.ObserveEmergencyContactsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SafetyDashboardViewModel @Inject constructor(
    private val authSession: AuthSession,
    private val dispatchers: DispatcherProvider,
    private val observeEmergencyContactsUseCase: ObserveEmergencyContactsUseCase
) : ViewModel() {

    private val _dashboardStatus = MutableStateFlow(DashboardStatus())
    val dashboardStatus = _dashboardStatus.asStateFlow()


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
        viewModelScope.launch {
            try {
                val uid = authSession.currentUid() ?: run {
                    onError()
                    return@launch
                }
                val contacts = withContext(dispatchers.io) {
                    observeEmergencyContactsUseCase(uid).first()
                }
                onResult(contacts)
            } catch (e: Exception) {
                onError()
            }
        }
    }

    fun checkEmergencyContactsExist() {
        viewModelScope.launch {
            try {
                val uid = authSession.currentUid() ?: return@launch
                val contacts = withContext(dispatchers.io) {
                    observeEmergencyContactsUseCase(uid).first()
                }
                val hasValidContact = contacts.any { it.phone.isNotBlank() }
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
