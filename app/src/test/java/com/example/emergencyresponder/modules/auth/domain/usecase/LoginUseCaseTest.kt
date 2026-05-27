package com.example.emergencyresponder.modules.auth.domain.usecase

import com.example.emergencyresponder.core.network.AuthException
import com.example.emergencyresponder.modules.auth.domain.model.AuthenticatedUser
import com.example.emergencyresponder.modules.auth.domain.repository.LoginRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginUseCaseTest {

    private lateinit var loginRepository: LoginRepository
    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setup() {
        loginRepository = mockk(relaxed = true)
        loginUseCase = LoginUseCase(loginRepository)
    }

    @Test
    fun `invoke calls repository login and returns AuthenticatedUser`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val expectedUser = AuthenticatedUser(uid = "uid1", name = "Test", email = email)

        coEvery { loginRepository.login(email, password) } returns expectedUser

        val result = loginUseCase(email, password)

        assertEquals(expectedUser, result)
        coVerify(exactly = 1) { loginRepository.login(email, password) }
    }

    @Test(expected = AuthException.InvalidCredentialsException::class)
    fun `invoke propagates AuthException from repository`() = runTest {
        coEvery { loginRepository.login(any(), any()) } throws AuthException.InvalidCredentialsException()

        loginUseCase("bad@test.com", "wrong")
    }

    @Test
    fun `executeGoogleLogin calls repository and returns AuthenticatedUser`() = runTest {
        val idToken = "google-id-token"
        val expectedUser = AuthenticatedUser(uid = "g1", name = "Google User", email = "g@test.com")

        coEvery { loginRepository.loginWithGoogle(idToken) } returns expectedUser

        val result = loginUseCase.executeGoogleLogin(idToken)

        assertEquals(expectedUser, result)
        coVerify(exactly = 1) { loginRepository.loginWithGoogle(idToken) }
    }
}
