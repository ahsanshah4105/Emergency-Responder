package com.example.emergencyresponder.modules.auth.ui.viewmodel

sealed class AuthUiEvent {
    data class ShowToast(val message: String) : AuthUiEvent()
    data class ShowSnackbar(val message: String) : AuthUiEvent()
    object VerificationEmailSent : AuthUiEvent()
    object PasswordResetEmailSent : AuthUiEvent()
}