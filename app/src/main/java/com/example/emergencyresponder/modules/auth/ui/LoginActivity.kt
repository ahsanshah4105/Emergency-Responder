package com.example.emergencyresponder.modules.auth.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.lifecycleScope
import com.example.emergencyresponder.R
import com.example.emergencyresponder.core.navigation.AppNavigator
import com.example.emergencyresponder.core.utils.ValidationUtils
import com.example.emergencyresponder.databinding.ActivityLoginBinding
import com.example.emergencyresponder.modules.auth.ui.viewmodel.AuthUiState
import com.example.emergencyresponder.modules.auth.ui.viewmodel.LoginViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupValidationListeners()
        setupListeners()
        setupObservers()
    }

    private fun setupObservers() {

        viewModel.state.observe(this) { state ->
            when (state) {
                is AuthUiState.Loading -> {
                    binding.btnProgressBar.visibility = View.VISIBLE
                }
                is AuthUiState.Success -> {
                    binding.btnProgressBar.visibility = View.GONE
                    Toast.makeText(this, state.resId, Toast.LENGTH_SHORT).show()
                }
                is AuthUiState.Error -> {
                    binding.btnProgressBar.visibility = View.GONE
                    Toast.makeText(this, state.resId.toString(), Toast.LENGTH_SHORT).show()
                    binding.loginEmailEditText.error = state.resId.toString()
                }
                is AuthUiState.Idle -> {
                    binding.btnProgressBar.visibility = View.GONE
                }
            }
        }
        viewModel.route.observe(this) { event ->
            event.getContentIfNotHandled()?.let { route ->
                AppNavigator.navigate(this, route, finishCurrent = true)
            }
        }
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.loginEmailEditText.text.toString()
            val password = binding.loginPasswordEditText.text.toString()

            viewModel.login(email, password)
        }

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

        val googleIdOption = GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id)).build()

        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(this@LoginActivity, request)
                handleSignInResult(result)
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, R.string.google_sign_in_cancelled, Toast.LENGTH_SHORT)
                    .show()
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

    private fun setupValidationListeners() {
        binding.loginEmailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val email = binding.loginEmailEditText.text.toString()
                binding.loginEmailEditText.error =
                    if (!ValidationUtils.isEmailValid(email)) "Invalid email" else null
            }
        }
        binding.loginPasswordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = binding.loginPasswordEditText.text.toString()
                binding.loginPasswordEditText.error =
                    if (!ValidationUtils.isNotEmpty(password)) "Password cannot be empty" else null
            }
        }
    }
}