package com.example.emergencyresponder.modules.splash.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.emergencyresponder.R
import com.example.emergencyresponder.core.navigation.AppNavigator
import com.example.emergencyresponder.core.navigation.AppRoute
import com.example.emergencyresponder.core.objects.SPreferenceManager


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        Handler(Looper.getMainLooper()).postDelayed({
            when {
                SPreferenceManager.isUserLoggedIn() -> {
                    AppNavigator.navigate(this, AppRoute.Dashboard)
                }

                SPreferenceManager.isOnboardingCompleted() -> {
                    AppNavigator.navigate(this, AppRoute.Login)
                }

                else -> {
                    AppNavigator.navigate(this, AppRoute.Onboarding)
                }
            }

        }, 3000)


    }

}