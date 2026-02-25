package com.example.emergencyresponder.modules.timestamp.data.repository

import com.example.emergencyresponder.core.constants.PrefKeys
import com.example.emergencyresponder.core.domain.repository.IBasePreference
import com.example.emergencyresponder.modules.timestamp.domain.repository.ICrashRepository

class ICrashRepositoryImpl(
    private val prefProvider: IBasePreference
) : ICrashRepository {


    override fun incrementCancelCount() {
        val current = getCancelCount()
        prefProvider.saveInt(PrefKeys.KEY_CANCEL_COUNT, current + 1)
    }

    override fun getCancelCount(): Int = prefProvider.getInt(PrefKeys.KEY_CANCEL_COUNT, 0)
}