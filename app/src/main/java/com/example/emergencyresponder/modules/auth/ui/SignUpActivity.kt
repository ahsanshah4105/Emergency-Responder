package com.example.emergencyresponder.modules.auth.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.emergencyresponder.core.navigation.AppRoute
import com.example.emergencyresponder.core.utils.ValidationUtils
import com.example.emergencyresponder.databinding.ActivitySignUpBinding
import com.example.emergencyresponder.modules.auth.domain.viewmodel.SignUpViewModel

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val viewModel: SignUpViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupValidationListeners()

        viewModel.route.observe(this) {
            if (it == AppRoute.Login) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun setupValidationListeners() {
        viewModel.emailError.observe(this) { binding.emailEditText.error = it }
        viewModel.passwordError.observe(this) { binding.confirmPassword.error = it }
        viewModel.nameError.observe(this) { binding.contactName.error = it }
        viewModel.phoneError.observe(this) { binding.emergencyContactPhone.error = it }

        binding.emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) viewModel.validateEmail(binding.emailEditText.text.toString())
        }


    }
    private fun setupListeners() {
        binding.signUptButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.newPassword.text.toString()
            val confirmPassword = binding.confirmPassword.text.toString()
            val name = binding.contactName.text.toString()
            val phone = binding.emergencyContactPhone.text.toString()

            if (!viewModel.validateInput(email, password, confirmPassword, name, phone)) {
                return@setOnClickListener
            }

            viewModel.signUp(email, password, confirmPassword, phone, name)
        }
    }
}

