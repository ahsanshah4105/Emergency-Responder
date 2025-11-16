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

class LoginActivity : AppCompatActivity() {

    private lateinit var checkBox: CheckBox
    private lateinit var forgotPassword: TextView
    private lateinit var signUp: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initializeViews()
        setupCheckbox()
        handleIntent()
    }


    private fun initializeViews() {
        checkBox = findViewById(R.id.checkbox)
        forgotPassword = findViewById(R.id.forgot_password)
        signUp = findViewById(R.id.signUp_layout)
    }

    private fun setupCheckbox() {
        val uncheckedColor = ContextCompat.getColor(this, R.color.unchecked_color)
        val checkedColor = ContextCompat.getColor(this, R.color.primaryColor)

        checkBox.buttonTintList = ColorStateList.valueOf(uncheckedColor)

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            val color = if (isChecked) checkedColor else uncheckedColor
            checkBox.buttonTintList = ColorStateList.valueOf(color)
        }
    }

    private fun handleIntent() {
        forgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        signUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}
