package com.example.emergencyresponder.modules.auth.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencyresponder.core.base.Event
import com.example.emergencyresponder.core.navigation.AppRoute
import com.example.emergencyresponder.core.manager.SPreferenceManager
import com.example.emergencyresponder.modules.auth.domain.usecase.LoginUseCase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableLiveData<AuthState>()
    val state: LiveData<AuthState> = _state

    private val _route = MutableLiveData< Event<AppRoute>>()
    val route: LiveData<Event<AppRoute>> = _route
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                loginUseCase(email, password)
                _state.value = AuthState.Success
                login(email, password)
                _route.value = Event(AppRoute.Dashboard)
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }
    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                loginUseCase.executeGoogleLogin(idToken)
                _state.value = AuthState.Success
                AppRoute.Dashboard
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Google Login failed")
            }
        }
    }

}

sealed class AuthState {
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
