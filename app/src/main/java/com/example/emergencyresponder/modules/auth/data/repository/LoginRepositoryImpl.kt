package com.example.emergencyresponder.modules.auth.data.repository

import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.domain.repository.LoginRepository
import com.google.firebase.auth.FirebaseUser

class LoginRepositoryImpl(
    private val authDataSource: AuthRemoteDataSource
) : LoginRepository {

    override suspend fun login(email: String, password: String): FirebaseUser {
        val result = authDataSource.loginUser(email, password)
        val user = result.user ?: throw Exception("User not found")
        return user
    }
}