package com.example.emergencyresponder.modules.auth.data.dataSource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await


class AuthRemoteDataSource(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun loginUser(email: String, password: String) =
        auth.signInWithEmailAndPassword(email, password).await()

    suspend fun createUser(email: String, password: String) =
        auth.createUserWithEmailAndPassword(email, password).await()

    suspend fun sendEmailVerification(user: FirebaseUser) =
        user.sendEmailVerification().await()

    fun logout() {
        auth.signOut()
    }
}
