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
import com.example.emergencyresponder.modules.auth.domain.useCase.SignUpUseCase
import com.example.emergencyresponder.modules.auth.domain.viewModelFactory.SignUpViewModelFactory
import com.example.emergencyresponder.modules.auth.domain.viewmodel.AuthState
import com.example.emergencyresponder.modules.auth.domain.viewmodel.SignUpViewModel

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
            val email = binding.emailEditText.text.toString()
            val password = binding.newPassword.text.toString()
            val confirmPassword = binding.confirmPassword.text.toString()
            val name = binding.contactName.text.toString()
            val phone = binding.emergencyContactPhone.text.toString()

            if (!viewModel.validateInput(name, email, password, confirmPassword, name, phone)) {
                return@setOnClickListener
            }

            viewModel.signUp(name, email, password, confirmPassword, phone, name)
        }
    }

}

