package com.example.emergencyresponder.modules.timestamp.ui.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.emergencyresponder.modules.timestamp.domain.repository.ICountdownManager
import com.example.emergencyresponder.modules.timestamp.domain.repository.ICrashRepository
import com.example.emergencyresponder.modules.timestamp.ui.viewmodel.EmergencyAlertViewModel

class TimeStampViewModelFactory(
    private val countdownManager: ICountdownManager,
    private val crashRepository: ICrashRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EmergencyAlertViewModel(
            countdownManager = countdownManager,
            crashRepository = crashRepository
        ) as T
    }
}

