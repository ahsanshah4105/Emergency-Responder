package com.example.emergencyresponder.modules.auth.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.emergencyresponder.core.navigation.AppNavigator
import com.example.emergencyresponder.core.navigation.AppRoute
import com.example.emergencyresponder.core.objects.SPreferenceManager
import com.example.emergencyresponder.core.utils.ValidationUtils
import com.example.emergencyresponder.databinding.ActivityLoginBinding
import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.repository.LoginRepositoryImpl
import com.example.emergencyresponder.modules.auth.domain.useCase.LoginUseCase
import com.example.emergencyresponder.modules.auth.domain.viewModelFactory.LoginViewModelFactory
import com.example.emergencyresponder.modules.auth.domain.viewmodel.AuthState
import com.example.emergencyresponder.modules.auth.domain.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val authRemoteDataSource = AuthRemoteDataSource()
        val repository = LoginRepositoryImpl(authRemoteDataSource)
        val useCase = LoginUseCase(repository)
        val factory = LoginViewModelFactory(useCase)
        viewModel = ViewModelProvider(this, factory).get(LoginViewModel::class.java)
        setupValidationListeners()
        setupListeners()

        viewModel.state.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> binding.btnProgressBar.visibility = View.VISIBLE
                is AuthState.Success -> {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    binding.btnProgressBar.visibility = View.INVISIBLE
                }
                is AuthState.Error -> {
                    Toast.makeText(this, "Make Sure Your Internet is Connected", Toast.LENGTH_SHORT).show()
                    binding.btnProgressBar.visibility = View.INVISIBLE
                }

            }
        }


        viewModel.route.observe(this) {
            if (it == AppRoute.Dashboard) {
                SPreferenceManager.setUserLoggedIn(true)

                AppNavigator.navigate(
                    activity = this,
                    route = AppRoute.Dashboard,
                    finishCurrent = true
                )
            }
        }

    }

    private fun setupValidationListeners() {
        binding.loginEmailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val email = binding.loginEmailEditText.text.toString()
                if (!ValidationUtils.isEmailValid(email)) {
                    binding.loginEmailEditText.error = "Invalid email"
                } else {
                    binding.loginEmailEditText.error = null
                }
            }
        }
        binding.loginPasswordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = binding.loginPasswordEditText.text.toString()
                if (!ValidationUtils.isNotEmpty(password)) {
                    binding.loginPasswordEditText.error = "Password cannot be empty"
                } else {
                    binding.loginPasswordEditText.error = null
                }
            }
        }
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.loginEmailEditText.text.toString()
            val password = binding.loginPasswordEditText.text.toString()

            if (!ValidationUtils.isEmailValid(email)) {
                binding.loginEmailEditText.error = "Invalid email"
                return@setOnClickListener
            }
            if (!ValidationUtils.isNotEmpty(password)) {
                binding.loginPasswordEditText.error = "Password cannot be empty"
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }

        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }
}