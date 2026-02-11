package com.example.emergencyresponder.core.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object SOSBlastManager {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun sendBlastToAllUsers(context: Context) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(context, "❌ Error: User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = currentUser.uid
        Toast.makeText(context, "Fetching contacts...", Toast.LENGTH_SHORT).show()

        // 1. Fetch Data from Firestore
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val uniqueNumbers = mutableSetOf<String>()

                    // 2. Extract Phone Numbers from the array
                    val contactsList = document.get("emergencyContacts") as? List<Map<String, String>>

                    contactsList?.forEach { contact ->
                        val phone = contact["phone"]
                        if (!phone.isNullOrEmpty()) {
                            uniqueNumbers.add(phone)
                        }
                    }

                    // 3. IF we found contacts, SEND THE ALERTS
                    if (uniqueNumbers.isNotEmpty()) {

                        // Convert Set to List (because SOSUtils expects a List)
                        val finalContactList = uniqueNumbers.toList()

                        Toast.makeText(context, "Sending to ${finalContactList.size} contacts...", Toast.LENGTH_SHORT).show()

                        // ---------------------------------------------------------
                        // 👇 HERE IS EXACTLY HOW SOSUTILS IS USED 👇
                        // ---------------------------------------------------------

                        SOSUtils.startEmergencySequence(context, uniqueNumbers.toList())


                        // ---------------------------------------------------------

                    } else {
                        Toast.makeText(context, "⚠️ You have 0 emergency contacts.", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(context, "❌ Profile not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}