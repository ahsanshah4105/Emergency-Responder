package com.example.emergencyresponder.modules.auth.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.emergencyresponder.core.navigation.AppRoute
import com.example.emergencyresponder.core.utils.BaseActivity
import com.example.emergencyresponder.databinding.ActivitySignUpBinding
import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.repository.SignUpRepositoryImpl
import com.example.emergencyresponder.modules.auth.domain.usecase.SignUpUseCase
import com.example.emergencyresponder.modules.auth.ui.viewModelFactory.SignUpViewModelFactory
import com.example.emergencyresponder.modules.auth.ui.viewmodel.AuthState
import com.example.emergencyresponder.modules.auth.ui.viewmodel.SignUpViewModel

class SignUpActivity : BaseActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var viewModel: SignUpViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val authRemoteDataSource = AuthRemoteDataSource()
        val userRemoteDataSource = UserRemoteDataSource()
        val repository = SignUpRepositoryImpl(
            authRemoteDataSource,
            userRemoteDataSource
        )
        val useCase = SignUpUseCase(repository)
        val factory = SignUpViewModelFactory(useCase)
        viewModel = ViewModelProvider(this, factory).get(SignUpViewModel::class.java)
        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()

            }
        setupListeners()
        setupValidationListeners()
        navigator()
        stateObserver()
    }

    private fun navigator() {
        viewModel.route.observe(this) {
            if (it == AppRoute.Login) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

    }

    private fun stateObserver() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.btnProgressBar.visibility = View.VISIBLE
                    binding.signUptButton.isEnabled = false
                }

                is AuthState.Success -> {
                    binding.btnProgressBar.visibility = View.GONE
                    binding.signUptButton.isEnabled = true
                }

                is AuthState.Error -> {
                    binding.btnProgressBar.visibility = View.GONE
                    binding.signUptButton.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun setupValidationListeners() {
        viewModel.userNameError.observe(this) {binding.contactName.error = it}
        viewModel.emailError.observe(this) { binding.emailEditText.error = it }
        viewModel.passwordError.observe(this) { binding.confirmPassword.error = it }
        viewModel.nameError.observe(this) { binding.contactName.error = it }
        viewModel.phoneError.observe(this) { binding.emergencyContactPhone.error = it }

        binding.userNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) viewModel.validateUserName(binding.userNameEditText.text.toString())
        }

        binding.emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) viewModel.validateEmail(binding.emailEditText.text.toString())
        }

        binding.contactName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) viewModel.validateName(binding.contactName.text.toString())
        }

        binding.emergencyContactPhone.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) viewModel.validatePhone(binding.emergencyContactPhone.text.toString().trim())
        }
    }
    private fun setupListeners() {
        binding.signUptButton.setOnClickListener {
            binding.btnProgressBar.visibility = View.VISIBLE
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.newPassword.text.toString().trim()
            val confirmPassword = binding.confirmPassword.text.toString().trim()

            val userName = binding.userNameEditText.text.toString().trim() // User's Name
            val contactName = binding.contactName.text.toString().trim()   // Emergency Contact Name
            val emergencyPhone = binding.emergencyContactPhone.text.toString().trim() // Emergency Phone
            if (!viewModel.validateInput(userName, email, password, confirmPassword, contactName, emergencyPhone)) {
                return@setOnClickListener
            }

            viewModel.signUp(
                userName = userName,
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                emergencyPhone = emergencyPhone,
                emergencyName = contactName
            )
        }


    }

}

