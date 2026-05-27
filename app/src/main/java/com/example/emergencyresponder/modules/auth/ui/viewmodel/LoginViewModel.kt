package com.example.emergencyresponder.modules.auth.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencyresponder.R
import com.example.emergencyresponder.core.base.Event
import com.example.emergencyresponder.core.navigation.AppRoute
import com.example.emergencyresponder.core.utils.ValidationUtils
import com.example.emergencyresponder.modules.auth.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    private val _state = MutableLiveData<AuthUiState>(AuthUiState.Idle)
    val state: LiveData<AuthUiState> = _state

    private val _route = MutableLiveData<Event<AppRoute>>()
    val route: LiveData<Event<AppRoute>> = _route

    fun login(email: String, password: String) {
        if (!ValidationUtils.isEmailValid(email)) {
            _state.value = AuthUiState.Error(R.string.invalid_email)
            return
        }

        viewModelScope.launch {
            _state.value = AuthUiState.Loading
            try {
                loginUseCase(email, password)
                _state.value = AuthUiState.Success(R.string.login_successful)
                _route.value = Event(AppRoute.Dashboard)
            } catch (e: Exception) {
                _state.value = AuthUiState.Error( e.message ?: R.string.login_failed)
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _state.value = AuthUiState.Loading
            try {
                loginUseCase.executeGoogleLogin(idToken)
                _state.value = AuthUiState.Success(R.string.login_successful)
                _route.value = Event(AppRoute.Dashboard)
            } catch (e: Exception) {
                _state.value = AuthUiState.Error(e.message ?: R.string.login_failed)
            }
        }
    }
}