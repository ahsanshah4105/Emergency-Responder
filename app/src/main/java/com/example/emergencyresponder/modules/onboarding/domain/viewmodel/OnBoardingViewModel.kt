package com.example.emergencyresponder.modules.onboarding.domain.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OnboardingViewModel : ViewModel() {
    private val _onboardingCompleted = MutableLiveData<Boolean>()
    val onboardingCompleted: LiveData<Boolean> = _onboardingCompleted
    private val _isCrashDetectionActive = MutableLiveData<Boolean>()
    val isCrashDetectionActive: LiveData<Boolean> = _isCrashDetectionActive
    fun isLastPage(current: Int, total: Int): Boolean {
        return current == total - 1
    }
    fun setCrashDetectionActive(active: Boolean) {
        _isCrashDetectionActive.value = active
    }

    fun startCrashDetection(current: Int, total: Int): Boolean {
        return current == total - 2
    }

    fun setOnboardingCompleted() {
        _onboardingCompleted.value = true
    }
}
