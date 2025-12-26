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
        val uid = authDataSource.createUser(email, password)
        userDataSource.saveUser(uid, user)
    }
}