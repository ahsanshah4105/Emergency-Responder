package com.example.emergencyresponder.modules.auth.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencyresponder.R
import com.example.emergencyresponder.modules.auth.domain.usecase.ForgotPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val forgotPasswordUseCase: ForgotPasswordUseCase
) : ViewModel() {

    private val _state = MutableLiveData<AuthUiState>()
    val state: LiveData<AuthUiState> = _state

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _state.value = AuthUiState.Loading
            try {
                forgotPasswordUseCase(email)
                _state.value = AuthUiState.Success(R.string.reset_password)
            } catch (e: Exception) {
                _state.value = AuthUiState.Error( R.string.failed_to_sent_reset_email)
            }
        }
    }
}