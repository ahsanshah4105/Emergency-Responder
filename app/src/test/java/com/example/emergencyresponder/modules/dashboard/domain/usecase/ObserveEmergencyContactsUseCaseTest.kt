package com.example.emergencyresponder.modules.dashboard.domain.usecase

import com.example.emergencyresponder.core.domain.model.EmergencyContact
import com.example.emergencyresponder.core.network.AuthException
import com.example.emergencyresponder.modules.dashboard.domain.repository.IEmergencyContactRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveEmergencyContactsUseCaseTest {

    private val repo: IEmergencyContactRepository = mockk()

    @Test(expected = AuthException.UserNotFoundException::class)
    fun `blank uid throws`() {
        val useCase = ObserveEmergencyContactsUseCase(repo)
        useCase("").also { /* should throw */ }
    }

    @Test
    fun `returns contacts sorted by name`() = runTest {
        val useCase = ObserveEmergencyContactsUseCase(repo)
        val input = listOf(
            EmergencyContact("Zed", "1"),
            EmergencyContact("Amy", "2"),
            EmergencyContact("Bob", "3")
        )
        every { repo.observeContacts("uid1") } returns flowOf(input)

        val result = useCase("uid1").first()

        assertEquals(listOf("Amy", "Bob", "Zed"), result.map { it.name })
    }
}

