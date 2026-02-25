package com.example.emergencyresponder.modules.timestamp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import com.example.emergencyresponder.core.base.Event
import com.example.emergencyresponder.modules.timestamp.domain.repository.ICountdownManager
import com.example.emergencyresponder.modules.timestamp.domain.repository.ICrashRepository

class EmergencyAlertViewModel(
    private val countdownManager: ICountdownManager,
    private val crashRepository: ICrashRepository,
) : ViewModel() {
    private val _finishActivity = MutableLiveData<Event<Unit>>()
    val finishActivity: LiveData<Event<Unit>> = _finishActivity

    val secondsRemaining: LiveData<Long> = countdownManager.remainingSeconds.asLiveData()

    val progress: LiveData<Int> = secondsRemaining.map { sec ->
        ((sec.toFloat() / countdownManager.totalTimeSec) * 100).toInt()
    }

    fun onUserIsOkay() {
        crashRepository.incrementCancelCount()
        countdownManager.cancel() // ViewModel ensures manager stops
        _finishActivity.value = Event(Unit)
    }
}