package com.example.emergencyresponder.modules.auth.ui.viewmodel

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val resId: Int) : AuthUiState
    data class Error(val resId: Any) : AuthUiState
}