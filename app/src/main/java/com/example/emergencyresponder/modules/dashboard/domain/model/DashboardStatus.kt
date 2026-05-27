package com.example.emergencyresponder.modules.dashboard.domain.model

/**
 * Domain model for dashboard feature status (audio, crash, snatch guards).
 */
data class DashboardStatus(
    val audio: Boolean = false,
    val crash: Boolean = false,
    val snatch: Boolean = false
)
