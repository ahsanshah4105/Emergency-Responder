package com.example.emergencyresponder.modules.splash.domain.repository

interface ISplashRepository {
    fun isUserLoggedIn(): Boolean
    fun isOnboardingCompleted(): Boolean
}