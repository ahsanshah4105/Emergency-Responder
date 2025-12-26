package com.example.emergencyresponder.modules.auth.data.dataSource

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await


class AuthRemoteDataSource(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun createUser(email: String, password: String): String {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw Exception("User ID is null")

        // Send email verification
        user.sendEmailVerification().await()

        return user.uid
    }

    suspend fun loginUser(email: String, password: String): String {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw Exception("User not found")
        if (!user.isEmailVerified) {
            throw Exception("Email not verified. Please check your email.")
        }
        return user.uid
    }

    fun logout() {
        auth.signOut()
    }
}
