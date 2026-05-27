package com.example.emergencyresponder.modules.auth.data.repository

import com.example.emergencyresponder.core.network.AuthException
import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.mapper.AuthMapper.toData
import com.example.emergencyresponder.modules.auth.domain.model.User
import com.example.emergencyresponder.modules.auth.domain.repository.SignUpRepository

class SignUpRepositoryImpl(
    private val authDataSource: AuthRemoteDataSource,
    private val userDataSource: UserRemoteDataSource
) : SignUpRepository {

    override suspend fun signUp(email: String, password: String, user: User) {
        val result = authDataSource.createUser(email, password)

        val firebaseUser = result.user ?: throw AuthException.UserCreationFailedException()

        val uid = firebaseUser.uid

        val userWithUid = user.copy(uid = uid)
        val dataUser = userWithUid.toData()

        // Save user first
        userDataSource.saveUser(uid, dataUser)

        // Then send verification
        authDataSource.sendEmailVerification()

        // Do NOT logout here
    }

}