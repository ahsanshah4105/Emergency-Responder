package com.example.emergencyresponder.modules.splash.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.emergencyresponder.core.navigation.AppNavigator
import com.example.emergencyresponder.core.navigation.AppRoute
import com.example.emergencyresponder.core.objects.SPreferenceManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.view.animation.AnimationUtils
import com.example.emergencyresponder.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Load animations
        val animLeft = AnimationUtils.loadAnimation(this, R.anim.from_left)
        val animRight = AnimationUtils.loadAnimation(this, R.anim.from_right)

        // Apply animations
        findViewById<ImageView>(R.id.imageView2).startAnimation(animLeft)
        findViewById<LinearLayout>(R.id.textLayout).startAnimation(animRight)

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
