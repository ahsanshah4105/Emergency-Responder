package com.example.emergencyresponder.modules.auth.domain.viewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.emergencyresponder.modules.auth.domain.useCase.SignUpUseCase
import com.example.emergencyresponder.modules.auth.domain.viewmodel.SignUpViewModel

class SignUpViewModelFactory(private val signUpUseCase: SignUpUseCase) : ViewModelProvider.Factory  {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SignUpViewModel(signUpUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}