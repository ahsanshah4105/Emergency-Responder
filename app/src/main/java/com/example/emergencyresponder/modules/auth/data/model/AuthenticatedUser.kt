package com.example.emergencyresponder.modules.auth.data.model

data class AuthenticatedUser(
    val uid: String,
    val name: String,
    val email: String
)