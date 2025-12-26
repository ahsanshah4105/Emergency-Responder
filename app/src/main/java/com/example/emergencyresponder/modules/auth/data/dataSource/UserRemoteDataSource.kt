package com.example.emergencyresponder.modules.auth.data.dataSource

import com.example.emergencyresponder.modules.auth.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class UserRemoteDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun saveUser(uid: String, user: User) {
        firestore.collection("Users")
            .document(uid)
            .set(user)
            .await()
    }
}