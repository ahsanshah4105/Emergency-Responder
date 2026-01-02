package com.example.emergencyresponder.core.utils

import com.google.i18n.phonenumbers.PhoneNumberUtil


object ValidationUtils {
    fun isPhoneValid(phone: String, defaultCountry: String = "PK"): Boolean {
        val phoneUtil = PhoneNumberUtil.getInstance()
        return try {
            val numberProto = phoneUtil.parse(phone, defaultCountry)
            phoneUtil.isValidNumber(numberProto)
        } catch (e: Exception) {
            false
        }
    }
    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPasswordMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    fun isNotEmpty(value: String): Boolean {
        return value.trim().isNotEmpty()
    }
}

