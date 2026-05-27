package com.example.emergencyresponder.modules.auth.domain.model

import com.example.emergencyresponder.core.domain.model.EmergencyContact

/**
 * Domain entity for user profile.
 */
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val emergencyContacts: List<EmergencyContact> = emptyList()
)
