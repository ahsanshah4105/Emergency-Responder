package com.example.emergencyresponder.core.domain.model

/**
 * Domain entity for an emergency contact.
 * Used across auth and dashboard features.
 */
data class EmergencyContact(
    val name: String = "",
    val phone: String = ""
)
