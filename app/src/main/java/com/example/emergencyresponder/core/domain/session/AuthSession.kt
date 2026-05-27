package com.example.emergencyresponder.core.domain.session

/**
 * Abstraction for current authentication session.
 * Keeps UI/ViewModels independent of FirebaseAuth.
 */
interface AuthSession {
    fun currentUid(): String?
}

