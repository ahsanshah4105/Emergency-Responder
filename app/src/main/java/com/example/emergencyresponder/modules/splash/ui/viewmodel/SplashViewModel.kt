package com.example.emergencyresponder.modules.splash.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.emergencyresponder.core.base.Event
import com.example.emergencyresponder.modules.splash.domain.repository.ISplashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(private val repository: ISplashRepository) : ViewModel() {

    sealed class SplashNavigation {
        object Dashboard : SplashNavigation()
        object Login : SplashNavigation()
        object Onboarding : SplashNavigation()
    }

    private val _navigationEvent = MutableLiveData<Event<SplashNavigation>>()
    val navigationEvent: LiveData<Event<SplashNavigation>> = _navigationEvent

    fun checkDestination() {
        val destination = when {
            repository.isUserLoggedIn() -> SplashNavigation.Dashboard
            repository.isOnboardingCompleted() -> SplashNavigation.Login
            else -> SplashNavigation.Onboarding
        }
        _navigationEvent.value = Event(destination)
    }
}