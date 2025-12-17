package com.example.emergencyresponder.modules.dashboard.data

data class EmergencyContacts(
    val iconRes: Int,
    val name: String,
    val phone: String,
    val sosAction: () -> Unit = {}
)
