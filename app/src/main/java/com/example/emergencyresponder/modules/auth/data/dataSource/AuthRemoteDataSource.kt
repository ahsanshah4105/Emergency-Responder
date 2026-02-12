package com.example.emergencyresponder.modules.auth.data.dataSource

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await


class AuthRemoteDataSource(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun loginUser(email: String, password: String) =
        auth.signInWithEmailAndPassword(email, password).await()

    suspend fun createUser(email: String, password: String) =
        auth.createUserWithEmailAndPassword(email, password).await()

    suspend fun sendEmailVerification() {
        val user = auth.currentUser
        user?.sendEmailVerification()?.await() ?: throw Exception("Verification Link has been sent to your email. Please verify your email to login.")
    }
    suspend fun verifyBeforeUpdateEmail(
        password: String,
        newEmail: String
    ) {
        val user = auth.currentUser ?: throw Exception("User not logged in")

        val currentEmail = user.email ?: throw Exception("Current email not found")

        // Reauthenticate with current credentials
        val credential = EmailAuthProvider.getCredential(currentEmail, password)
        try {
            user.reauthenticate(credential).await()
        } catch (e: Exception) {
            throw Exception("Current password is incorrect or session expired")
        }

        // Send verification to new email
        user.verifyBeforeUpdateEmail(newEmail).await()
    }

    suspend fun sendPasswordResetEmail(email: String) =
        auth.sendPasswordResetEmail(email).await()

    suspend fun loginWithGoogle(idToken: String): AuthResult {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return FirebaseAuth.getInstance().signInWithCredential(credential).await()
    }
    fun logout() {
        auth.signOut()
    }
}
