package com.example.emergencyresponder.modules.auth.data.repository

import com.example.emergencyresponder.core.network.AuthException
import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.model.User
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

        // Save user first
        userDataSource.saveUser(uid, userWithUid)

        // Then send verification
        authDataSource.sendEmailVerification()

        // Do NOT logout here
    }

}