package com.example.emergencyresponder.core.data.session

import com.example.emergencyresponder.core.domain.session.AuthSession
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class FirebaseAuthSession @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthSession {
    override fun currentUid(): String? = firebaseAuth.currentUser?.uid
}

