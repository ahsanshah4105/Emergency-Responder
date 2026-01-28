package com.example.emergencyresponder.modules.auth.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val emergencyContacts: List<EmergencyContact> = emptyList()
)
