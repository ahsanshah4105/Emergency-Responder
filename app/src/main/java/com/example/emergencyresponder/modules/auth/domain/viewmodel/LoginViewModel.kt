package com.example.emergencyresponder.modules.auth.domain.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencyresponder.core.navigation.AppRoute
import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.repository.LoginRepositoryImpl
import com.example.emergencyresponder.modules.auth.domain.useCase.LoginUseCase
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase = LoginUseCase(LoginRepositoryImpl(
        authDataSource = AuthRemoteDataSource()
    ))
) : ViewModel() {

    private val _state = MutableLiveData<AuthState>()
    val state: LiveData<AuthState> = _state

    private val _route = MutableLiveData<AppRoute>()
    val route: LiveData<AppRoute> = _route

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                loginUseCase(email, password)
                _state.value = AuthState.Success
                _route.value = AppRoute.Dashboard
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }
}