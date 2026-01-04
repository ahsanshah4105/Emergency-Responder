package com.example.emergencyresponder.core.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.emergencyresponder.modules.auth.ui.ForgotPasswordActivity
import com.example.emergencyresponder.modules.auth.ui.LoginActivity
import com.example.emergencyresponder.modules.auth.ui.SignUpActivity
import com.example.emergencyresponder.modules.dashboard.ui.SafetyDashboardActivity
import com.example.emergencyresponder.modules.onboarding.ui.OnboardingActivity
import com.example.emergencyresponder.modules.timestamp.ui.TimeStampActivity

object AppNavigator {

    fun navigate(
        context: Context,
        route: AppRoute,
        finishCurrent: Boolean = true
    ) {
        val intent = when (route) {
            AppRoute.Login -> Intent(context, LoginActivity::class.java)
            AppRoute.SignUp -> Intent(context, SignUpActivity::class.java)
            AppRoute.ForgotPassword -> Intent(context, ForgotPasswordActivity::class.java)
            AppRoute.Dashboard -> Intent(context, SafetyDashboardActivity::class.java)
            AppRoute.TimeStamp -> Intent(context, TimeStampActivity::class.java)
            AppRoute.Onboarding -> Intent(context, OnboardingActivity::class.java)
        }

        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)

        if (finishCurrent && context is Activity) {
            context.finish()
        }
    }
}
