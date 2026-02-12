package com.example.emergencyresponder.modules.dashboard.data.datasource

import com.example.emergencyresponder.modules.auth.data.model.EmergencyContact
import com.example.emergencyresponder.modules.auth.data.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class EmergencyContactRemoteDataSource(
    private val firestore: FirebaseFirestore
) {

    fun observeContacts(
        uid: String,
        onUpdate: (List<EmergencyContact>) -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onError(error.message ?: "Unknown Error")
                    return@addSnapshotListener
                }

                val user = snapshot?.toObject(User::class.java)
                onUpdate(user?.emergencyContacts ?: emptyList())
            }
    }

    fun addContact(
        uid: String,
        contact: EmergencyContact,
        onResult: (Boolean) -> Unit
    ) {
        firestore.collection("users")
            .document(uid)
            .update("emergencyContacts", FieldValue.arrayUnion(contact))
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun deleteContact(
        uid: String,
        contact: EmergencyContact,
        onResult: (Boolean) -> Unit
    ) {
        // arrayRemove requires the exact object to find and remove it
        firestore.collection("users")
            .document(uid)
            .update("emergencyContacts", FieldValue.arrayRemove(contact))
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener {
                // If the document doesn't exist or network fails
                onResult(false)
            }
    }

    // NEW: Logic to send an SOS alert to the backend
    fun sendSOSNotification(
        uid: String,
        contact: EmergencyContact,
        location: String,
        onResult: (Boolean) -> Unit
    ) {
        // Create an alert object to store in Firestore
        val sosAlert = hashMapOf(
            "senderUid" to uid,
            "targetContactName" to contact.name,
            "targetContactPhone" to contact.phone,
            "location" to location,
            "status" to "SENT",
            "timestamp" to FieldValue.serverTimestamp()
        )

        firestore.collection("sos_alerts")
            .add(sosAlert)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}
