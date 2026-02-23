package com.example.emergencyresponder.modules.onboarding.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OnboardingViewModel : ViewModel() {
    private val _onboardingCompleted = MutableLiveData<Boolean>()
    val onboardingCompleted: LiveData<Boolean> = _onboardingCompleted
    fun isLastPage(current: Int, total: Int): Boolean {
        return current == total - 1
    }

    fun setOnboardingCompleted() {
        _onboardingCompleted.value = true
    }
}
