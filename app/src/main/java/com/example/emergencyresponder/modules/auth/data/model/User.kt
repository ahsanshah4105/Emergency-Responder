package com.example.emergencyresponder.modules.auth.data.model


data class User(
    val name: String,
    val email: String,
    val uid: String, // Added this to match your repository logic
    val phone: String? = null,
    val emergencyName: String? = null,
    // Sensitive data like passwords usually shouldn't live in the User profile model
    val password: String? = null,
    val confirmPassword: String? = null
)