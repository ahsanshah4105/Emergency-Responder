package com.example.emergencyresponder.modules.auth.data.repository

import com.example.emergencyresponder.core.network.AuthException
import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.model.User
import com.example.emergencyresponder.modules.auth.domain.repository.LoginRepository
import com.example.emergencyresponder.modules.auth.domain.repository.UserPreferences
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class LoginRepositoryImpl(
    private val authDataSource: AuthRemoteDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val prefs: UserPreferences
) : LoginRepository {

    override suspend fun login(email: String, password: String): User {
        try {

            val result = authDataSource.loginUser(email, password)
            val firebaseUser =
                result.user ?: throw AuthException.UserNotFoundException() as Throwable

            if (!firebaseUser.isEmailVerified) {
                authDataSource.logout()
                throw AuthException.EmailNotVerifiedException()
            }

            val user = userRemoteDataSource.getUser(firebaseUser.uid)
            prefs.saveUserSession(
                uid = user.uid,
                name = user.name,
                email = user.email,
            )

            prefs.setUserLoggedIn(true)

            return user

        } catch (e: FirebaseAuthInvalidCredentialsException) {
            throw AuthException.InvalidCredentialsException()
        } catch (e: Exception) {
            throw AuthException.NetworkException()
        }

    }

    override suspend fun loginWithGoogle(idToken: String): User {
        try {

            val result = authDataSource.loginWithGoogle(idToken)

            val firebaseUser = result.user ?: throw AuthException.GoogleLoginException()

            val user = User(
                uid = firebaseUser.uid,
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",

                emergencyContacts = emptyList()
            )
            prefs.saveUserSession(uid = user.uid, name = user.name, email = user.email)
            prefs.setUserLoggedIn(true)
            userRemoteDataSource.saveUserOnlyIfNew(user)

            return user
        } catch (e: Exception) {
            throw AuthException.NetworkException()
        }
    }

}