package com.example.emergencyresponder.modules.timestamp.domain.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.os.CountDownTimer

class TimeStampViewModel : ViewModel() {

    private val _secondsRemaining = MutableLiveData<Int>()
    val secondsRemaining: LiveData<Int> get() = _secondsRemaining

    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> get() = _progress

    private var timer: CountDownTimer? = null

    fun startCountdown(totalSeconds: Int = 60) {
        timer?.cancel() // cancel previous if exists

        val totalMillis = totalSeconds * 1000L
        timer = object : CountDownTimer(totalMillis, 1000L) {

            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                _secondsRemaining.value = seconds
                _progress.value = (seconds.toFloat() / totalSeconds * 100).toInt()
            }

            override fun onFinish() {
                _secondsRemaining.value = 0
                _progress.value = 0
            }

        }.start()
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}
