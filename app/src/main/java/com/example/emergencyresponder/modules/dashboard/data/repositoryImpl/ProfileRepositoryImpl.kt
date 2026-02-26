package com.example.emergencyresponder.modules.dashboard.data.repositoryImpl

import com.example.emergencyresponder.core.network.AuthException
import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.dashboard.domain.repository.IProfileRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class ProfileRepositoryImpl(
    private val userDataSource: UserRemoteDataSource,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : IProfileRepository {

    override suspend fun updateProfile(uid: String, name: String, email: String) {
        val finalUid = uid.ifEmpty { auth.currentUser?.uid ?: "" }

        if (finalUid.isEmpty()) throw AuthException.UserSessionIsExpiredException()

        val updates = mapOf("name" to name, "email" to email)
        userDataSource.updateUserDetails(finalUid, updates)
    }

    override suspend fun updatePassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser ?: throw AuthException.UserNotAuthenticatedException()
        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
        user.reauthenticate(credential).await()
        user.updatePassword(newPassword).await()
    }

    override suspend fun changeEmail(password: String, newEmail: String) {
        val user = auth.currentUser ?: throw AuthException.UserNotAuthenticatedException()
        val credential = EmailAuthProvider.getCredential(user.email!!, password)
        user.reauthenticate(credential).await()
        user.verifyBeforeUpdateEmail(newEmail).await()
    }
}
//class ProfileRepositoryImpl(
//    private val userDataSource: UserRemoteDataSource,
//    private val authDataSource: AuthRemoteDataSource
//) : ProfileRepository {
//
//    override suspend fun updateUserProfile(uid: String, name: String, email: String) {
//        val updates = mapOf(
//            "name" to name,
//            "email" to email
//        )
//        userDataSource.updateUserDetails(uid, updates)
//    }
//
//    override suspend fun requestEmailChange(
//        currentEmail: String,
//        password: String,
//        newEmail: String
//    ) {
//        authDataSource.verifyBeforeUpdateEmail(
//            password,
//            newEmail
//        )
//    }
//}