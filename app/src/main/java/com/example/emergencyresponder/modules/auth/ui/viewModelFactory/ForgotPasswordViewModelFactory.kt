package com.example.emergencyresponder.modules.auth.ui.viewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.emergencyresponder.modules.auth.domain.usecase.ResetPasswordUseCase
import com.example.emergencyresponder.modules.auth.ui.viewmodel.ForgotPasswordViewModel


class ForgotPasswordViewModelFactory(private val resetPasswordUseCase: ResetPasswordUseCase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ForgotPasswordViewModel(resetPasswordUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}