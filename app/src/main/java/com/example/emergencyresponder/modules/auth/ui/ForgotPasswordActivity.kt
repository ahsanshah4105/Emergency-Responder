package com.example.emergencyresponder.modules.auth.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.example.emergencyresponder.R
import com.example.emergencyresponder.core.base.BaseActivity
import com.example.emergencyresponder.databinding.ActivityForgotPasswordBinding
import com.example.emergencyresponder.modules.auth.ui.viewmodel.AuthUiState
import com.example.emergencyresponder.modules.auth.ui.viewmodel.ForgotPasswordViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordActivity : BaseActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                    Toast.makeText(this, R.string.reset_link_sent, Toast.LENGTH_LONG).show()
                    finish()
                }
                is AuthUiState.Error -> {
                    binding.btnProgressBar.visibility = View.GONE
                    Toast.makeText(this, state.resId.toString(), Toast.LENGTH_SHORT).show()
                }

                is AuthUiState.Idle -> binding.btnProgressBar.visibility = View.GONE
            }
        }
    }
}