package com.example.emergencyresponder.modules.auth.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.emergencyresponder.R // Ensure this is your project's R
import com.example.emergencyresponder.core.navigation.AppNavigator
import com.example.emergencyresponder.core.navigation.AppRoute
import com.example.emergencyresponder.core.objects.SPreferenceManager
import com.example.emergencyresponder.core.utils.ValidationUtils
import com.example.emergencyresponder.databinding.ActivityLoginBinding
import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.repository.LoginRepositoryImpl
import com.example.emergencyresponder.modules.auth.domain.useCase.LoginUseCase
import com.example.emergencyresponder.modules.auth.domain.viewModelFactory.LoginViewModelFactory
import com.example.emergencyresponder.modules.auth.domain.viewmodel.AuthState
import com.example.emergencyresponder.modules.auth.domain.viewmodel.LoginViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Manual Injection (Consider using Hilt/Koin in the future)
        val authRemoteDataSource = AuthRemoteDataSource()
        val remoteDataSource = UserRemoteDataSource()
        val repository = LoginRepositoryImpl(authRemoteDataSource,remoteDataSource)
        val useCase = LoginUseCase(repository)
        val factory = LoginViewModelFactory(useCase)
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        setupValidationListeners()
        setupListeners()
        setupObservers()
    }

    private fun setupObservers() {

        viewModel.state.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.btnProgressBar.visibility = View.VISIBLE
                }
                is AuthState.Success -> {
                    binding.btnProgressBar.visibility = View.INVISIBLE
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                }
                is AuthState.Error -> {
                    binding.btnProgressBar.visibility = View.INVISIBLE
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe Navigation/Routing
        viewModel.route.observe(this) { route ->
            if (route == AppRoute.Dashboard) {
                SPreferenceManager.setUserLoggedIn(true)
                AppNavigator.navigate(
                    context = this,
                    route = AppRoute.Dashboard,
                    finishCurrent = true
                )
            }
        }
    }

    private fun setupListeners() {
        // Standard Login
        binding.loginButton.setOnClickListener {
            val email = binding.loginEmailEditText.text.toString()
            val password = binding.loginPasswordEditText.text.toString()

            if (validateInput(email, password)) {
                viewModel.login(email, password)
            }
        }

        // Google Login Button (Make sure this ID exists in your XML)
        binding.loginWithGoogle.setOnClickListener {
            launchGoogleSignIn()
        }

        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun launchGoogleSignIn() {
        val credentialManager = CredentialManager.create(this)

        // Use your Web Client ID from Firebase Console / Google Cloud Console
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(this@LoginActivity, request)
                handleSignInResult(result)
            } catch (e: Exception) {
                Log.e("Auth", "Google Sign-In failed: ${e.message}")
                Toast.makeText(this@LoginActivity, "Google Sign-In Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSignInResult(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                viewModel.loginWithGoogle(idToken)
            } catch (e: Exception) {
                Log.e("Auth", "Error parsing Google Credential", e)
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true
        if (!ValidationUtils.isEmailValid(email)) {
            binding.loginEmailEditText.error = "Invalid email"
            isValid = false
        }
        if (!ValidationUtils.isNotEmpty(password)) {
            binding.loginPasswordEditText.error = "Password cannot be empty"
            isValid = false
        }
        return isValid
    }

    private fun setupValidationListeners() {
        binding.loginEmailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val email = binding.loginEmailEditText.text.toString()
                binding.loginEmailEditText.error = if (!ValidationUtils.isEmailValid(email)) "Invalid email" else null
            }
        }
        binding.loginPasswordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = binding.loginPasswordEditText.text.toString()
                binding.loginPasswordEditText.error = if (!ValidationUtils.isNotEmpty(password)) "Password cannot be empty" else null
            }
        }
    }
}