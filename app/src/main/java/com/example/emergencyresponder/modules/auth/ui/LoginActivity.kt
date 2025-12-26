package com.example.emergencyresponder.modules.auth.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.emergencyresponder.R
import com.example.emergencyresponder.core.navigation.AppRoute
import com.example.emergencyresponder.core.utils.ValidationUtils
import com.example.emergencyresponder.databinding.ActivityLoginBinding
import com.example.emergencyresponder.databinding.ActivityOnboardingBinding
import com.example.emergencyresponder.modules.auth.domain.viewmodel.AuthState
import com.example.emergencyresponder.modules.auth.domain.viewmodel.LoginViewModel
import com.example.emergencyresponder.modules.dashboard.ui.SafetyDashboardActivity
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupValidationListeners()
        setupListeners()

        viewModel.state.observe(this) { state ->
            when (state) {
                is AuthState.Success -> Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                is AuthState.Error -> Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
            }
        }


        viewModel.route.observe(this) {
            if (it == AppRoute.Dashboard) {
                startActivity(Intent(this, SafetyDashboardActivity::class.java))
                finish()
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
    }
}