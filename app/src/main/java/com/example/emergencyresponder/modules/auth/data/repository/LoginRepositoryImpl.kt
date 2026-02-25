package com.example.emergencyresponder.modules.auth.data.repository

import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.model.User
import com.example.emergencyresponder.modules.auth.domain.repository.LoginRepository
import com.example.emergencyresponder.modules.auth.domain.repository.UserPreferences

class LoginRepositoryImpl(
    private val authDataSource: AuthRemoteDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val prefs: UserPreferences
) : LoginRepository {

    override suspend fun login(email: String, password: String): User {
        // 1. Authenticate with Firebase
        val result = authDataSource.loginUser(email, password)
        val firebaseUser = result.user ?: throw Exception("Login failed")

        // 2. CHECK VERIFICATION HERE
        if (!firebaseUser.isEmailVerified) {
            // Optional: Logout immediately so they aren't stuck in a half-logged-in state
            authDataSource.logout()
            throw Exception("Email not verified. Please check your inbox.")
        }

        // 3. Fetch User Details from Firestore
        // (Assuming you added getUser() to UserRemoteDataSource as discussed)
        val user = userRemoteDataSource.getUser(firebaseUser.uid)
        // userRemoteDataSource.saveUserOnlyIfNew(user)

        // 4. Save to SharedPreferences
        prefs.saveUserSession(
            uid = user.uid,
            name = user.name,
            email = user.email,
        )

        prefs.setUserLoggedIn(true)

        return user
    }
    override suspend fun loginWithGoogle(idToken: String): User {

        val result = authDataSource.loginWithGoogle(idToken)

        val firebaseUser = result.user
            ?: throw Exception("Google login failed")

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
    }

}