package com.example.emergencyresponder.modules.auth.data.repository

import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.auth.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val dataSource: UserRemoteDataSource
) : ProfileRepository {

    override suspend fun updateUserProfile(uid: String, name: String, email: String) {
        // Create a map of the fields we want to change
        val updates = mapOf(
            "name" to name,
            "phone" to email
        )
        dataSource.updateUserDetails(uid, updates)
    }
}