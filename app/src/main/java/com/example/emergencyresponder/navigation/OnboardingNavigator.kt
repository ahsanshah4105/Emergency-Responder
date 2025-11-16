package com.example.emergencyresponder.navigation


import android.app.Activity
import android.content.Intent
import com.example.emergencyresponder.modules.auth.ui.LoginActivity
import com.example.emergencyresponder.modules.onboarding.ui.OnboardingActivity

object OnboardingNavigator {

    fun goToLogin(activity: Activity) {
        activity.startActivity(Intent(activity, LoginActivity::class.java))
        activity.finish()
    }

    fun goToOnboarding(activity: Activity) {
        activity.startActivity(Intent(activity, OnboardingActivity::class.java))
        activity.finish()
    }
}
