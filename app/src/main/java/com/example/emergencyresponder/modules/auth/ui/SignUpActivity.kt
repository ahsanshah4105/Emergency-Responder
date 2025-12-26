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
        binding.emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val email = binding.emailEditText.text.toString()
                if (!ValidationUtils.isEmailValid(email)) {
                    binding.emailEditText.error = "Invalid email"
                }
            } else {
                binding.emailEditText.error = null
            }
        }

        binding.confirmPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = binding.newPassword.text.toString()
                val confirmPassword = binding.confirmPassword.text.toString()
                if (!ValidationUtils.isPasswordMatch(password, confirmPassword)) {
                    binding.confirmPassword.error = "Passwords do not match"
                }
            } else {
                binding.confirmPassword.error = null
            }
        }

        binding.newPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = binding.newPassword.text.toString()
                val confirmPassword = binding.confirmPassword.text.toString()
                if (!ValidationUtils.isPasswordMatch(password, confirmPassword)) {
                    binding.confirmPassword.error = "Passwords do not match"
                }
            } else {
                binding.newPassword.error = null
            }
        }

        binding.contactName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val name = binding.contactName.text.toString()
                if (!ValidationUtils.isNotEmpty(name)) {
                    binding.contactName.error = "Name cannot be empty"
                }
            }
        }

        binding.emergencyContactPhone.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val phone = binding.emergencyContactPhone.text.toString()
                if (!ValidationUtils.isPhoneValid(phone)) {
                    binding.emergencyContactPhone.error = "Invalid phone number"
                }
            }
        }
    }

    private fun setupListeners() {
        binding.signUptButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val newPassword = binding.newPassword.text.toString()
            val confirmPassword = binding.confirmPassword.text.toString()
            val emergencyContactName = binding.contactName.text.toString()
            val emergencyContactPhone = binding.emergencyContactPhone.text.toString()

            if (!ValidationUtils.isNotEmpty(emergencyContactName)) {
                binding.contactName.error = "Name cannot be empty"
                return@setOnClickListener
            }
            if (!ValidationUtils.isPhoneValid(emergencyContactPhone)) {
                binding.emergencyContactPhone.error = "Invalid phone number"
                return@setOnClickListener
            }

            viewModel.signUp(
                email = email,
                password = newPassword,
                confirmPassword = confirmPassword,
                phone = emergencyContactPhone,
                emergencyName = emergencyContactName,
            )

        }
    }
}

