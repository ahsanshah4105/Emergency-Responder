package com.example.emergencyresponder.modules.auth.data.repository

import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.domain.repository.ResetPasswordRepository

class ResetPasswordRepositoryImpl (
    private val authDataSource: AuthRemoteDataSource
    ) : ResetPasswordRepository {
    override suspend fun resetPassword(email: String) {
        authDataSource.sendPasswordResetEmail(email)
    }
}