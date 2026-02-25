package com.example.emergencyresponder.modules.auth.ui.viewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.emergencyresponder.modules.auth.domain.usecase.ForgotPasswordUseCase
import com.example.emergencyresponder.modules.auth.ui.viewmodel.ForgotPasswordViewModel


class ForgotPasswordViewModelFactory(private val forgotPasswordUseCase: ForgotPasswordUseCase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ForgotPasswordViewModel(forgotPasswordUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}