package com.example.emergencyresponder.modules.auth.data.repository

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

        if (result.user != null) {
            authDataSource.sendEmailVerification()

            userDataSource.saveUser(result.user!!.uid, user)

            authDataSource.logout()
        } else {
            throw Exception("User creation failed")
        }
    }
}