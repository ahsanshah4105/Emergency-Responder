package com.example.emergencyresponder.modules.dashboard.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.emergencyresponder.core.domain.coroutines.DispatcherProvider
import com.example.emergencyresponder.core.domain.model.EmergencyContact
import com.example.emergencyresponder.core.domain.session.AuthSession
import com.example.emergencyresponder.modules.dashboard.domain.usecase.ObserveEmergencyContactsUseCase
import com.example.emergencyresponder.testutil.MainDispatcherRule
import com.example.emergencyresponder.testutil.getOrAwaitValue
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SafetyDashboardViewModelTest {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val authSession: AuthSession = mockk()
    private val dispatchers: DispatcherProvider = mockk {
        every { main } returns UnconfinedTestDispatcher()
        every { io } returns UnconfinedTestDispatcher()
        every { default } returns UnconfinedTestDispatcher()
    }
    private val observeUseCase: ObserveEmergencyContactsUseCase = mockk()

    private lateinit var viewModel: SafetyDashboardViewModel

    @Before
    fun setup() {
        every { authSession.currentUid() } returns "uid1"
    }

    @Test
    fun `checkEmergencyContactsExist navigates when no valid contacts`() = runTest {
        every { observeUseCase("uid1") } returns flowOf(emptyList())
        viewModel = SafetyDashboardViewModel(authSession, dispatchers, observeUseCase)

        viewModel.checkEmergencyContactsExist()

        val shouldNavigate = viewModel.navigateToEmergencyContacts.getOrAwaitValue()
        assertEquals(true, shouldNavigate)
    }

    @Test
    fun `checkEmergencyContactsExist does not navigate when contact exists`() = runTest {
        val contacts = listOf(EmergencyContact("Dad", "+123"))
        every { observeUseCase("uid1") } returns flowOf(contacts)
        viewModel = SafetyDashboardViewModel(authSession, dispatchers, observeUseCase)

        viewModel.checkEmergencyContactsExist()

        // LiveData may remain null if no navigation requested; default is null.
        val value = viewModel.navigateToEmergencyContacts.value
        assertEquals(null, value)
    }
}

