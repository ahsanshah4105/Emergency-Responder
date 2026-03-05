package com.example.emergencyresponder.modules.auth.data.dataSource

import com.example.emergencyresponder.core.network.AuthException
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
        val user = auth.currentUser ?: throw AuthException.UserNotFoundException()
        try {
            user.sendEmailVerification().await()
        } catch (e: Exception) {
            throw AuthException.EmailProviderException()
        }
    }

    suspend fun verifyBeforeUpdateEmail(
        password: String,
        newEmail: String
    ) {
        val user = auth.currentUser ?: throw AuthException.UserNotFoundException()


        val currentEmail = user.email ?: throw AuthException.UserNotLoggedInException()

        val credential = EmailAuthProvider.getCredential(currentEmail, password)
        try {
            user.reauthenticate(credential).await()
        } catch (e: Exception) {
            throw AuthException.InvalidCredentialsException()
        }
        user.verifyBeforeUpdateEmail(newEmail).await()
        logout()
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
