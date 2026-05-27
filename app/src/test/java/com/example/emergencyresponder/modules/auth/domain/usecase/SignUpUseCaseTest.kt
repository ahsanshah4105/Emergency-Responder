package com.example.emergencyresponder.modules.auth.domain.usecase

import com.example.emergencyresponder.core.domain.model.EmergencyContact
import com.example.emergencyresponder.modules.auth.domain.model.User
import com.example.emergencyresponder.modules.auth.domain.repository.SignUpRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpUseCaseTest {

    private lateinit var signUpRepository: SignUpRepository
    private lateinit var signUpUseCase: SignUpUseCase

    @Before
    fun setup() {
        signUpRepository = mockk(relaxed = true)
        signUpUseCase = SignUpUseCase(signUpRepository)
    }

    @Test
    fun `invoke calls repository signUp with user`() = runTest {
        val email = "new@example.com"
        val password = "pass123"
        val user = User(
            uid = "",
            name = "New User",
            email = email,
            emergencyContacts = listOf(EmergencyContact("Dad", "+1234567890"))
        )

        coEvery { signUpRepository.signUp(email, password, user) } returns Unit

        signUpUseCase(email, password, user)

        coVerify(exactly = 1) { signUpRepository.signUp(email, password, user) }
    }
}
