package com.example.emergencyresponder.core.utils


object ValidationUtils {
    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPasswordMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    fun isNotEmpty(value: String): Boolean {
        return value.trim().isNotEmpty()
    }

    fun isPhoneValid(phone: String): Boolean {
        // Simple validation: must be 10-15 digits
        return phone.matches(Regex("^[0-9]{10,15}\$"))
    }
}
