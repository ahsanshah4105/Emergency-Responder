package com.example.emergencyresponder.modules.auth.domain.service

import com.google.firebase.auth.FirebaseUser


interface LoginRepository {
    suspend fun login(email: String, password: String): FirebaseUser
}
