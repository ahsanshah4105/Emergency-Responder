package com.example.emergencyresponder.modules.auth.data.repository

import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.auth.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val userDataSource: UserRemoteDataSource,
    private val authDataSource: AuthRemoteDataSource
) : ProfileRepository {

    override suspend fun updateUserProfile(uid: String, name: String, email: String) {
        val updates = mapOf(
            "name" to name,
            "email" to email
        )
        userDataSource.updateUserDetails(uid, updates)
    }

    override suspend fun requestEmailChange(
        currentEmail: String,
        password: String,
        newEmail: String
    ) {
        authDataSource.verifyBeforeUpdateEmail(
            password,
            newEmail
        )
    }
}

