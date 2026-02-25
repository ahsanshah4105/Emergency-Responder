package com.example.emergencyresponder.modules.onboarding.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.emergencyresponder.core.base.Event
import com.example.emergencyresponder.modules.onboarding.domain.repository.IOnboardingRepository


class OnboardingViewModel(
    private val repository: IOnboardingRepository
) : ViewModel() {

    // Senior Way: Define clear events for the View
    sealed class OnboardingNavigation {
        object NavigateToLogin : OnboardingNavigation()
        object RequestPermission : OnboardingNavigation()
        data class MoveToNextPage(val nextIndex: Int) : OnboardingNavigation()
    }

    private val _navigationEvent = MutableLiveData<Event<OnboardingNavigation>>()
    val navigationEvent: LiveData<Event<OnboardingNavigation>> = _navigationEvent

    /**
     * Vetted Platform Standard:
     * The ViewModel decides WHAT to do, Activity only knows HOW to show it.
     */
    fun onContinueClicked(currentPage: Int, totalItems: Int, hasPermission: Boolean) {
        val isLastPage = currentPage == totalItems - 1

        when {
            isLastPage && hasPermission -> {
                repository.completeOnboarding()
                _navigationEvent.value = Event(OnboardingNavigation.NavigateToLogin)
            }
            isLastPage && !hasPermission -> {
                _navigationEvent.value = Event(OnboardingNavigation.RequestPermission)
            }
            else -> {
                _navigationEvent.value = Event(OnboardingNavigation.MoveToNextPage(currentPage + 1))
            }
        }
    }
}