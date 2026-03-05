package com.example.emergencyresponder.modules.auth.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.emergencyresponder.core.base.BaseActivity
import com.example.emergencyresponder.databinding.ActivityForgotPasswordBinding
import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.repository.ForgotPasswordRepositoryImpl
import com.example.emergencyresponder.modules.auth.domain.usecase.ForgotPasswordUseCase
import com.example.emergencyresponder.modules.auth.ui.viewModelFactory.ForgotPasswordViewModelFactory
import com.example.emergencyresponder.modules.auth.ui.viewmodel.AuthUiState
import com.example.emergencyresponder.modules.auth.ui.viewmodel.ForgotPasswordViewModel

class ForgotPasswordActivity : BaseActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var viewModel: ForgotPasswordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = ForgotPasswordRepositoryImpl(AuthRemoteDataSource())
        val useCase = ForgotPasswordUseCase(repository)
        viewModel = ViewModelProvider(this, ForgotPasswordViewModelFactory(useCase))[ForgotPasswordViewModel::class.java]

        setupObservers()

        binding.resetPasswordButton.setOnClickListener {
            val email = binding.resetPasswordEmail.text.toString().trim()
            viewModel.resetPassword(email)
        }
    }

    private fun setupObservers() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is AuthUiState.Loading -> binding.btnProgressBar.visibility = View.VISIBLE
                is AuthUiState.Success -> {
                    binding.btnProgressBar.visibility = View.GONE
                    Toast.makeText(this, "Reset link sent to your email!", Toast.LENGTH_LONG).show()
                    finish()
                }
                is AuthUiState.Error -> {
                    binding.btnProgressBar.visibility = View.GONE
                    Toast.makeText(this, state.resId.toString(), Toast.LENGTH_SHORT).show()
                }

                AuthUiState.Idle -> binding.btnProgressBar.visibility = View.GONE
            }
        }
    }
}