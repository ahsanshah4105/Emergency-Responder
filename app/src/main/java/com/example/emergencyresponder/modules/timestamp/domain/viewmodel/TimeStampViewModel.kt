package com.example.emergencyresponder.modules.timestamp.domain.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.os.CountDownTimer

class TimeStampViewModel : ViewModel() {

    private val _secondsRemaining = MutableLiveData<Int>()
    val secondRemaining: LiveData<Int> get() = _secondsRemaining


    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> get() = _progress

    private var timer: CountDownTimer? = null


    // ✅ Add helper function to update manually
    fun updateCountdown(seconds: Int) {
        _secondsRemaining.value = seconds
        _progress.value = ((seconds.toFloat() / 60) * 100).toInt()
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}
