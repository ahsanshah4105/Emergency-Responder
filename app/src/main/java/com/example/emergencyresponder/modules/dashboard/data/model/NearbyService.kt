package com.example.emergencyresponder.modules.dashboard.data.model

data class NearbyService(
    val iconRes: Int,
    val title: String,
    val location: String,
    val distance: String,
    val sosAction: () -> Unit = {}
)