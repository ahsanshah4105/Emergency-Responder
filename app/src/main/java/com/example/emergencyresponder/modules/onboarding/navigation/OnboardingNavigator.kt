package com.example.emergencyresponder.modules.onboarding.navigation


import android.app.Activity
import android.content.Intent
import com.example.emergencyresponder.modules.auth.ui.LoginActivity

object OnboardingNavigator {

    fun goToLogin(activity: Activity) {
        activity.startActivity(Intent(activity, LoginActivity::class.java))
        activity.finish()
    }
}
