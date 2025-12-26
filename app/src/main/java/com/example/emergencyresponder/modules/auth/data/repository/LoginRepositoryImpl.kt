package com.example.emergencyresponder.modules.auth.data.repository

import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.domain.service.LoginRepository

class LoginRepositoryImpl(
    private val authDataSource: AuthRemoteDataSource
) : LoginRepository {
    override suspend fun login(email: String, password: String) {
        authDataSource.loginUser(email, password)
    }
}