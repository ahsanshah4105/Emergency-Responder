package com.example.emergencyresponder.modules.splash.ui.viewmodelfactory


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.emergencyresponder.modules.splash.domain.repository.ISplashRepository
import com.example.emergencyresponder.modules.splash.ui.viewmodel.SplashViewModel

class SplashViewModelFactory(
    private val repository: ISplashRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SplashViewModel(repository) as T
    }
}