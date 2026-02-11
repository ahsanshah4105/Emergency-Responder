package com.example.emergencyresponder.modules.auth.data.repository

import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.auth.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val dataSource: UserRemoteDataSource
) : ProfileRepository {

    override suspend fun updateUserProfile(uid: String, name: String, email: String) {
        val updates = mapOf(
            "name" to name,
            "email" to email // ✅ Correct field name
        )
        dataSource.updateUserDetails(uid, updates)
    }
}
