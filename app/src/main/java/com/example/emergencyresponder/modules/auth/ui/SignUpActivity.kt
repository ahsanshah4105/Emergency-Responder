package com.example.emergencyresponder.modules.auth.ui

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.emergencyresponder.R

class SignUpActivity : AppCompatActivity() {

    private lateinit var loginButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        initializeViews()
        handleIntent()
    }

    private fun initializeViews() {
        loginButton = findViewById(R.id.login_button)

    }

    private fun handleIntent() {
        loginButton.setOnClickListener {
            finish()
        }
    }
}