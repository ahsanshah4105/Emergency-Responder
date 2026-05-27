package com.example.emergencyresponder.modules.auth.domain.model

/**
 * Domain entity representing an authenticated user session.
 */
data class AuthenticatedUser(
    val uid: String,
    val name: String,
    val email: String
)
