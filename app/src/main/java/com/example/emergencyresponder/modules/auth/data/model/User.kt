package com.example.emergencyresponder.modules.auth.data.model

data class User(
    val  name: String,
    val email: String,
    val password: String,
    val confirmPassword: String?,
    val phone: String,
    val emergencyName: String,
)