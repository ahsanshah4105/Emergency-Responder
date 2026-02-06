package com.example.emergencyresponder.modules.auth.data.repository

import com.example.emergencyresponder.core.objects.SPreferenceManager
import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.model.EmergencyContact
import com.example.emergencyresponder.modules.auth.data.model.User
import com.example.emergencyresponder.modules.auth.domain.repository.LoginRepository
import com.google.firebase.auth.FirebaseUser

class LoginRepositoryImpl(
    private val authDataSource: AuthRemoteDataSource,
    private val userRemoteDataSource: UserRemoteDataSource
) : LoginRepository {

// In modules/auth/data/repository/LoginRepositoryImpl.kt

    override suspend fun login(email: String, password: String): User {
        // 1. Authenticate with Firebase
        val result = authDataSource.loginUser(email, password)
        val firebaseUser = result.user ?: throw Exception("Login failed")

        // 2. CHECK VERIFICATION HERE
        if (!firebaseUser.isEmailVerified) {
            // Optional: Logout immediately so they aren't stuck in a half-logged-in state
            authDataSource.logout()
            throw Exception("Email not verified. Please check your inbox.")
        }

        // 3. Fetch User Details from Firestore
        // (Assuming you added getUser() to UserRemoteDataSource as discussed)
        val user = userRemoteDataSource.getUser(firebaseUser.uid)
       // userRemoteDataSource.saveUserOnlyIfNew(user)

        // 4. Save to SharedPreferences
        SPreferenceManager.saveUserSession(
            uid = user.uid,
            name = user.name,
            email = user.email,
        )

        return user
    }
    override suspend fun loginWithGoogle(idToken: String): User {

        val result = authDataSource.loginWithGoogle(idToken)

        val firebaseUser = result.user
            ?: throw Exception("Google login failed")

        val user = User(
            uid = firebaseUser.uid,
            name = firebaseUser.displayName ?: "Google User",
            email = firebaseUser.email ?: "",
        )
        userRemoteDataSource.saveUserOnlyIfNew(user)
        SPreferenceManager.saveUserSession(
            uid = user.uid,
            name = user.name,
            email = user.email,
        )
        return user
    }




//    // In LoginRepositoryImpl.kt
//    override suspend fun loginWithGoogle(idToken: String): User {
//        try {
//            val result = authDataSource.loginWithGoogle(idToken)
//            val firebaseUser = result.user ?: throw Exception("Google Login failed: No user found")
//
//            // Using named arguments makes this much safer and clearer
//            return User(
//                uid = firebaseUser.uid,
//                email = firebaseUser.email ?: "",
//                name = firebaseUser.displayName ?: "Google User",
//                phone = firebaseUser.phoneNumber ?: ""   // ✅ FIX HERE
//            )
//
//
//        } catch (e: Exception) {
//            throw Exception(e.message ?: "Authentication with Firebase failed")
//        }
//    }
}