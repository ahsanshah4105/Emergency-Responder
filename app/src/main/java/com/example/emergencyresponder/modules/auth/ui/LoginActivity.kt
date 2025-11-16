package com.example.emergencyresponder.modules.auth.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.emergencyresponder.R
import com.example.emergencyresponder.databinding.ActivityLoginBinding
import com.example.emergencyresponder.databinding.ActivityOnboardingBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var checkBox: CheckBox
    private lateinit var forgotPassword: TextView
    private lateinit var signUp: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //initializeViews()
        setupCheckbox()
        handleIntent()
    }


    private fun initializeViews() {
        checkBox = findViewById(R.id.checkbox)
        forgotPassword = findViewById(R.id.forgot_password)
        signUp = findViewById(R.id.register_button)
    }

    private fun setupCheckbox() {
        val uncheckedColor = ContextCompat.getColor(this, R.color.unchecked_color)
        val checkedColor = ContextCompat.getColor(this, R.color.primaryColor)

        binding.checkbox.buttonTintList = ColorStateList.valueOf(uncheckedColor)

        binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            val color = if (isChecked) checkedColor else uncheckedColor
            checkBox.buttonTintList = ColorStateList.valueOf(color)
        }
    }

    private fun handleIntent() {
        binding.forgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        binding.registerButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}
