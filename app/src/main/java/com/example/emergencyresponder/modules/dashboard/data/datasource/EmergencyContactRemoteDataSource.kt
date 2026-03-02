package com.example.emergencyresponder.modules.dashboard.data.datasource

import com.example.emergencyresponder.modules.auth.data.model.EmergencyContact
import com.example.emergencyresponder.modules.auth.data.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EmergencyContactRemoteDataSource(
    private val firestore: FirebaseFirestore
) {
    fun observeContacts(uid: String): Flow<List<EmergencyContact>> = callbackFlow {
        val subscription = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(User::class.java)
                val contacts = user?.emergencyContacts ?: emptyList()
                trySend(contacts)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addContact(uid: String, contact: EmergencyContact) {
        firestore.collection("users").document(uid)
            .update("emergencyContacts", FieldValue.arrayUnion(contact))
            .await()
    }

    suspend fun deleteContact(uid: String, contact: EmergencyContact) {
        firestore.collection("users").document(uid)
            .update("emergencyContacts", FieldValue.arrayRemove(contact))
            .await()
    }

    suspend fun sendSOSNotification(uid: String, contact: EmergencyContact, location: String) {
        val sosAlert = hashMapOf(
            "senderUid" to uid,
            "targetContactName" to contact.name,
            "targetContactPhone" to contact.phone,
            "location" to location,
            "status" to "SENT",
            "timestamp" to FieldValue.serverTimestamp()
        )
        firestore.collection("sos_alerts").add(sosAlert).await()
    }
}