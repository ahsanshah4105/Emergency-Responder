package com.example.emergencyresponder.modules.auth.data.repository

import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.domain.repository.ForgotPasswordRepository

class ForgotPasswordRepositoryImpl (
    private val authDataSource: AuthRemoteDataSource,
    ) : ForgotPasswordRepository {
    override suspend fun resetPassword(email: String) {
        authDataSource.sendPasswordResetEmail(email)
    }
}