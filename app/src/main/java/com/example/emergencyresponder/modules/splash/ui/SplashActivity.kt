package com.example.emergencyresponder.modules.splash.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.emergencyresponder.R
import com.example.emergencyresponder.core.objects.SPreferenceManager
import com.example.emergencyresponder.navigation.OnboardingNavigator
import com.example.emergencyresponder.modules.onboarding.ui.OnboardingActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler(Looper.getMainLooper()).postDelayed({
            if (SPreferenceManager.isOnboardingCompleted()) {
                OnboardingNavigator.goToLogin(this)
            } else {
                OnboardingNavigator.goToOnboarding(this)
            }

            finish()
        }, 3000)


    }

}