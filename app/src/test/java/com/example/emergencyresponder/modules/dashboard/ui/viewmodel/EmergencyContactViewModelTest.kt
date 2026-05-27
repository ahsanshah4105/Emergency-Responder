package com.example.emergencyresponder.modules.dashboard.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.emergencyresponder.core.base.Event
import com.example.emergencyresponder.core.domain.coroutines.DispatcherProvider
import com.example.emergencyresponder.core.domain.model.EmergencyContact
import com.example.emergencyresponder.core.domain.session.AuthSession
import com.example.emergencyresponder.modules.dashboard.domain.usecase.AddEmergencyContactUseCase
import com.example.emergencyresponder.modules.dashboard.domain.usecase.DeleteEmergencyContactUseCase
import com.example.emergencyresponder.modules.dashboard.domain.usecase.ObserveEmergencyContactsUseCase
import com.example.emergencyresponder.testutil.MainDispatcherRule
import com.example.emergencyresponder.testutil.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmergencyContactViewModelTest {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val authSession: AuthSession = mockk()
    private val dispatchers: DispatcherProvider = mockk {
        every { main } returns UnconfinedTestDispatcher()
        every { io } returns UnconfinedTestDispatcher()
        every { default } returns UnconfinedTestDispatcher()
    }
    private val observeUseCase: ObserveEmergencyContactsUseCase = mockk()
    private val addUseCase: AddEmergencyContactUseCase = mockk()
    private val deleteUseCase: DeleteEmergencyContactUseCase = mockk()

    private lateinit var viewModel: EmergencyContactViewModel

    @Before
    fun setup() {
        every { authSession.currentUid() } returns "uid1"
    }

    @Test
    fun `init observes contacts and posts list`() {
        val contacts = listOf(EmergencyContact("A", "1"), EmergencyContact("B", "2"))
        every { observeUseCase("uid1") } returns flowOf(contacts)

        viewModel = EmergencyContactViewModel(
            authSession = authSession,
            dispatchers = dispatchers,
            observeUseCase = observeUseCase,
            addUseCase = addUseCase,
            deleteUseCase = deleteUseCase
        )

        val result = viewModel.contacts.getOrAwaitValue()
        assertEquals(contacts, result)
    }

    @Test
    fun `addContact calls use case with uid`() {
        val contact = EmergencyContact("Dad", "+123")
        every { observeUseCase("uid1") } returns flowOf(emptyList())
        coEvery { addUseCase("uid1", contact) } returns Unit

        viewModel = EmergencyContactViewModel(authSession, dispatchers, observeUseCase, addUseCase, deleteUseCase)
        viewModel.addContact(contact)

        coVerify(exactly = 1) { addUseCase("uid1", contact) }
    }

    @Test
    fun `deleteContact calls use case with uid`() {
        val contact = EmergencyContact("Dad", "+123")
        every { observeUseCase("uid1") } returns flowOf(emptyList())
        coEvery { deleteUseCase("uid1", contact) } returns Unit

        viewModel = EmergencyContactViewModel(authSession, dispatchers, observeUseCase, addUseCase, deleteUseCase)
        viewModel.deleteContact(contact)

        coVerify(exactly = 1) { deleteUseCase("uid1", contact) }
    }

    @Test
    fun `when uid missing posts UNKNOWN error event`() {
        every { authSession.currentUid() } returns null
        every { observeUseCase(any()) } returns flowOf(emptyList())

        viewModel = EmergencyContactViewModel(authSession, dispatchers, observeUseCase, addUseCase, deleteUseCase)

        val event: Event<EmergencyError> = viewModel.error.getOrAwaitValue()
        val err = event.getContentIfNotHandled()
        assertTrue(err == EmergencyError.UNKNOWN)
    }
}

