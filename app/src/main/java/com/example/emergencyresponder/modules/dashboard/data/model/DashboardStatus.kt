package com.example.emergencyresponder.modules.dashboard.data.model

data class DashboardStatus(
    val audio: Boolean = false,
    val crash: Boolean = false, // location + notification
    val snatch: Boolean = false
)
