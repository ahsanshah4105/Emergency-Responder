package com.example.emergencyresponder.modules.auth.data.repository

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

    override suspend fun login(email: String, password: String): FirebaseUser {
        val result = authDataSource.loginUser(email, password)
        val user = result.user ?: throw Exception("User not found")
        return user
    }
    override suspend fun loginWithGoogle(idToken: String): User {

        val result = authDataSource.loginWithGoogle(idToken)

        val firebaseUser = result.user
            ?: throw Exception("Google login failed")

        val user = User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            name = firebaseUser.displayName ?: ""
        )
        userRemoteDataSource.saveUserOnlyIfNew(user)

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