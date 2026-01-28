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
}
