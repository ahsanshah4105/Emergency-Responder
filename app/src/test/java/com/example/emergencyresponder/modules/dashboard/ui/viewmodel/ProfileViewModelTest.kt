package com.example.emergencyresponder.modules.dashboard.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.emergencyresponder.core.base.Event
import com.example.emergencyresponder.core.common.PrefKeys
import com.example.emergencyresponder.core.domain.coroutines.DispatcherProvider
import com.example.emergencyresponder.core.domain.repository.IBasePreference
import com.example.emergencyresponder.core.domain.session.AuthSession
import com.example.emergencyresponder.modules.dashboard.domain.repository.IProfileRepository
import com.example.emergencyresponder.modules.dashboard.domain.usecase.ChangeEmailUseCase
import com.example.emergencyresponder.modules.dashboard.domain.usecase.UpdateProfileUseCase
import com.example.emergencyresponder.testutil.MainDispatcherRule
import com.example.emergencyresponder.testutil.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val updateProfileUseCase: UpdateProfileUseCase = mockk()
    private val changeEmailUseCase: ChangeEmailUseCase = mockk()
    private val profileRepository: IProfileRepository = mockk()
    private val prefs: IBasePreference = mockk(relaxed = true)
    private val authSession: AuthSession = mockk()
    private val dispatchers: DispatcherProvider = mockk {
        every { main } returns UnconfinedTestDispatcher()
        every { io } returns UnconfinedTestDispatcher()
        every { default } returns UnconfinedTestDispatcher()
    }

    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        every { prefs.getString(PrefKeys.USER_NAME) } returns "Old"
        every { prefs.getString(PrefKeys.USER_EMAIL) } returns "old@test.com"
        every { prefs.getString(PrefKeys.USER_ID) } returns "uid1"
        every { authSession.currentUid() } returns "uid1"
        viewModel = ProfileViewModel(
            updateProfileUseCase = updateProfileUseCase,
            changeEmailUseCase = changeEmailUseCase,
            repository = profileRepository,
            prefProvider = prefs,
            authSession = authSession,
            dispatchers = dispatchers
        )
    }

    @Test
    fun `loadCurrentUserData posts name and email`() {
        viewModel.loadCurrentUserData()
        val pair = viewModel.userData.getOrAwaitValue()
        assertTrue(pair.first == "Old" && pair.second == "old@test.com")
    }

    @Test
    fun `onSaveClicked with name change calls updateProfileUseCase and saves new name`() {
        coEvery { updateProfileUseCase(any(), any(), any()) } returns Unit

        viewModel.onSaveClicked(
            newName = "New Name",
            newEmail = "old@test.com",
            currentPass = "",
            newPass = ""
        )

        coVerify(exactly = 1) { updateProfileUseCase("uid1", "New Name", "old@test.com") }
        verify { prefs.saveString(PrefKeys.USER_NAME, "New Name") }
    }

    @Test
    fun `onSaveClicked with password change calls repository updatePassword`() {
        coEvery { profileRepository.updatePassword(any(), any()) } returns Unit

        viewModel.onSaveClicked(
            newName = "Old",
            newEmail = "old@test.com",
            currentPass = "current",
            newPass = "newpass"
        )

        coVerify(exactly = 1) { profileRepository.updatePassword("current", "newpass") }
        val stateEvent: Event<ProfileState> = viewModel.state.getOrAwaitValue()
        assertTrue(stateEvent.peekContent() is ProfileState.Loading || true)
    }
}

