package com.example.emergencyresponder.modules.auth.data.model

data class User(
    val email: String,
    val password: String,
    val confirmPassword: String?,
    val phone: String,
    val emergencyName: String,
)