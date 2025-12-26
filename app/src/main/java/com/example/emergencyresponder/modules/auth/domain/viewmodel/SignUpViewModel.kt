package com.example.emergencyresponder.modules.auth.domain.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencyresponder.core.navigation.AppRoute
import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.model.User
import com.example.emergencyresponder.modules.auth.data.repository.SignUpRepositoryImpl
import com.example.emergencyresponder.modules.auth.domain.useCase.SignUpUseCase
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val signUpUseCase: SignUpUseCase = SignUpUseCase(
        SignUpRepositoryImpl(
            authDataSource = AuthRemoteDataSource(),
            userDataSource = UserRemoteDataSource()
        )
    )
) : ViewModel() {

    private val _state = MutableLiveData<AuthState>()
    val state: LiveData<AuthState> = _state

    private val _route = MutableLiveData<AppRoute>()
    val route: LiveData<AppRoute> = _route

    fun signUp(email: String, password: String, confirmPassword: String, phone: String, emergencyName: String) {
        viewModelScope.launch {
            try {
                val user = User(
                    email, password, confirmPassword, phone, emergencyName
                )
                signUpUseCase(email, password, user)
                _state.value = AuthState.Success
                _route.value = AppRoute.Login
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Signup failed")
            }
        }
    }
}

sealed class AuthState {
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}