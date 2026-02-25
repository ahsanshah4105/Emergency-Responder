package com.example.emergencyresponder.modules.auth.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencyresponder.modules.auth.domain.usecase.ForgotPasswordUseCase
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val forgotPasswordUseCase: ForgotPasswordUseCase
) : ViewModel() {

    private val _state = MutableLiveData<AuthState>()
    val state: LiveData<AuthState> = _state

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                forgotPasswordUseCase(email)
                _state.value = AuthState.Success("Password reset email sent")
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Failed to send reset email")
            }
        }
    }
}