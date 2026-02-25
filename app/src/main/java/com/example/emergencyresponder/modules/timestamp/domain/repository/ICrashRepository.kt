package com.example.emergencyresponder.modules.timestamp.domain.repository

interface ICrashRepository {
    fun incrementCancelCount()
    fun getCancelCount(): Int
}