package com.example.emergencyresponder.modules.auth.domain.viewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.emergencyresponder.modules.auth.domain.useCase.LoginUseCase
import com.example.emergencyresponder.modules.auth.domain.viewmodel.LoginViewModel

class LoginViewModelFactory(private val loginUseCase: LoginUseCase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(loginUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}