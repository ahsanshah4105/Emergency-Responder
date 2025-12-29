package com.example.emergencyresponder.modules.onboarding.domain.viewmodel

import androidx.lifecycle.ViewModel


class OnboardingViewModel : ViewModel() {


    fun isLastPage(current: Int, total: Int): Boolean {
        return current == total - 1
    }
}
