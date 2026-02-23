package com.example.emergencyresponder.modules.auth.domain.usecase

import com.example.emergencyresponder.core.utils.ValidationUtils


data class ValidationResult(val isValid: Boolean, val errorMessage: String? = null)

class Validator {

    fun validateEmail(email: String): ValidationResult {
        return if (ValidationUtils.isEmailValid(email)) ValidationResult(true)
        else ValidationResult(false, "Invalid email")
    }

    fun validatePassword(password: String, confirmPassword: String): ValidationResult {
        return if (ValidationUtils.isPasswordMatch(password, confirmPassword)) ValidationResult(true)
        else ValidationResult(false, "Passwords do not match")
    }

    fun validateName(name: String): ValidationResult {
        return if (ValidationUtils.isNotEmpty(name)) ValidationResult(true)
        else ValidationResult(false, "Name cannot be empty")
    }

    fun validatePhone(phone: String): ValidationResult {
        return if (ValidationUtils.isPhoneValid(phone)) ValidationResult(true)
        else ValidationResult(false, "Invalid phone number")
    }
}
