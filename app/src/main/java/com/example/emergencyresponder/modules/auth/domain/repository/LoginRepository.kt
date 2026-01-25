package com.example.emergencyresponder.modules.auth.domain.repository

import com.example.emergencyresponder.modules.auth.data.model.User
import com.google.firebase.auth.FirebaseUser


interface LoginRepository {
    suspend fun login(email: String, password: String): FirebaseUser
    suspend fun loginWithGoogle(idToken: String): User
}
