package com.example.emergencyresponder.modules.auth.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.emergencyresponder.modules.auth.domain.model.AuthenticatedUser
import com.example.emergencyresponder.modules.auth.domain.usecase.LoginUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var loginUseCase: LoginUseCase
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        loginUseCase = mockk(relaxed = true)
        viewModel = LoginViewModel(loginUseCase)
    }

    @Test
    fun `login with valid email calls use case and updates state`() = runTest {
        val email = "test@example.com"
        val password = "pass123"
        val user = AuthenticatedUser(uid = "u1", name = "Test", email = email)

        coEvery { loginUseCase(email, password) } returns user

        viewModel.login(email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is AuthUiState.Success || state is AuthUiState.Loading)
    }

    @Test
    fun `login with invalid email does not call use case`() = runTest {
        viewModel.login("not-an-email", "password")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { loginUseCase(any(), any()) }
    }
}
