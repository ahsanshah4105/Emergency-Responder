package com.example.emergencyresponder.modules.timestamp.data.repository

import com.example.emergencyresponder.core.domain.repository.IBasePreference
import com.example.emergencyresponder.modules.timestamp.domain.repository.ICrashRepository

class ICrashRepositoryImpl(
    private val prefProvider: IBasePreference
) : ICrashRepository {

    companion object {
        private const val KEY_CANCEL_COUNT = "cancel_count_monitor"
    }

    override fun incrementCancelCount() {
        val current = getCancelCount()
        prefProvider.saveInt(KEY_CANCEL_COUNT, current + 1)
    }

    override fun getCancelCount(): Int = prefProvider.getInt(KEY_CANCEL_COUNT, 0)
}