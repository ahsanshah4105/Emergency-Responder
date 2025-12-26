package com.example.emergencyresponder.modules.auth.data.repository

import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.model.User
import com.example.emergencyresponder.modules.auth.domain.service.SignUpRepository


class SignUpRepositoryImpl(
    private val authDataSource: AuthRemoteDataSource,
    private val userDataSource: UserRemoteDataSource
) : SignUpRepository {

    override suspend fun signUp(email: String, password: String, user: User) {
        val result = authDataSource.createUser(email, password)
        val firebaseUser = result.user ?: throw Exception("User not created")
        authDataSource.sendEmailVerification(firebaseUser)
        userDataSource.saveUser(firebaseUser.uid, user)
    }
}