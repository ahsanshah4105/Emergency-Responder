package com.example.emergencyresponder.modules.dashboard.domain.usecase

import com.example.emergencyresponder.core.network.AuthException
import com.example.emergencyresponder.core.domain.model.EmergencyContact
import com.example.emergencyresponder.modules.dashboard.domain.repository.IEmergencyContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class ObserveEmergencyContactsUseCase(
    private val repository: IEmergencyContactRepository
) {
    operator fun invoke(uid: String): Flow<List<EmergencyContact>> {
        if (uid.isBlank()) {
            throw AuthException.UserNotFoundException()
        }

        return repository.observeContacts(uid)
            .map { list ->
                list.sortedBy { it.name }
            }
            .catch { e ->
                throw AuthException.DatabaseException("${e.message}")
            }
    }
}