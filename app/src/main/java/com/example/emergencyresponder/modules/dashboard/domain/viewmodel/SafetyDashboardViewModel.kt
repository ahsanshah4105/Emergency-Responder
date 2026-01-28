package com.example.emergencyresponder.modules.dashboard.domain.viewmodel

import androidx.lifecycle.ViewModel
import com.example.emergencyresponder.modules.dashboard.data.model.DashboardStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SafetyDashboardViewModel : ViewModel() {

    private val _dashboardStatus = MutableStateFlow(DashboardStatus())
    val dashboardStatus = _dashboardStatus.asStateFlow()

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

}
