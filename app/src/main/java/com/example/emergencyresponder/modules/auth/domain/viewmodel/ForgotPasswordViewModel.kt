package com.example.emergencyresponder.modules.auth.domain.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencyresponder.modules.auth.domain.useCase.ResetPasswordUseCase
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    private val _state = MutableLiveData<AuthState>()
    val state: LiveData<AuthState> = _state

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                resetPasswordUseCase(email)
                _state.value = AuthState.Success
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Failed to send reset email")
            }
        }
    }
}