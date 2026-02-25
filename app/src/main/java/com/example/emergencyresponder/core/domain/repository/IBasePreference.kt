package com.example.emergencyresponder.core.domain.repository


interface IBasePreference {
    fun saveString(key: String, value: String?)
    fun getString(key: String, default: String = ""): String

    fun saveBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, default: Boolean = false): Boolean

    fun saveInt(key: String, value: Int)
    fun getInt(key: String, default: Int = 0): Int

    fun remove(key: String)
    fun clearUserSession() // Specially for Logout
}