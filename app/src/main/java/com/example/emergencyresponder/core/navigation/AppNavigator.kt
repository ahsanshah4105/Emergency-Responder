package com.example.emergencyresponder.core.navigation

import android.app.Activity
import android.content.Intent
import com.example.emergencyresponder.modules.auth.ui.ForgotPasswordActivity
import com.example.emergencyresponder.modules.auth.ui.LoginActivity
import com.example.emergencyresponder.modules.auth.ui.SignUpActivity
import com.example.emergencyresponder.modules.dashboard.ui.SafetyDashboardActivity
import com.example.emergencyresponder.modules.onboarding.ui.OnboardingActivity
import com.example.emergencyresponder.modules.timestamp.ui.TimeStampActivity

object AppNavigator {

    fun navigate(
        activity: Activity,
        route: AppRoute,
        finishCurrent: Boolean = true
    ) {
        val intent = when (route) {
            AppRoute.Login -> Intent(activity, LoginActivity::class.java)
            AppRoute.SignUp -> Intent(activity, SignUpActivity::class.java)
            AppRoute.ForgotPassword -> Intent(activity, ForgotPasswordActivity::class.java)
            AppRoute.Dashboard -> Intent(activity, SafetyDashboardActivity::class.java)
            AppRoute.TimeStamp -> Intent(activity, TimeStampActivity::class.java)
            AppRoute.Onboarding -> Intent(activity, OnboardingActivity::class.java)
        }

        activity.startActivity(intent)

        if (finishCurrent) {
            activity.finish()
        }
    }
}
