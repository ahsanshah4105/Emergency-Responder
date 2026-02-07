package com.example.emergencyresponder.modules.auth.data.dataSource

import com.example.emergencyresponder.modules.auth.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await


class UserRemoteDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun saveUser(uid: String, user: User) {
        firestore.collection("users")
            .document(uid)
            .set(user)
            .await()
    }
    suspend fun saveUserOnlyIfNew(user: User) {

        val docRef = firestore.collection("users").document(user.uid)

        val snapshot = docRef.get().await()

        if (!snapshot.exists()) {
            // ✅ Save only first time
            docRef.set(user).await()
        }
    }
    suspend fun getUser(uid: String): User {
        val snapshot = firestore.collection("users").document(uid).get().await()
        return snapshot.toObject(User::class.java)
            ?: throw Exception("User data not found in database")
    }
    suspend fun updateUserDetails(uid: String, updates: Map<String, Any>) {
        firestore.collection("users")
            .document(uid)
            .update(updates) // .update() changes only specific fields
            .await()
    }
}