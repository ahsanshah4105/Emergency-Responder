package com.example.emergencyresponder.core.navigation

sealed class AppRoute {
    object Login : AppRoute()
    object SignUp : AppRoute()
    object ForgotPassword : AppRoute()
    object Dashboard : AppRoute()
    object TimeStamp : AppRoute()
    object Onboarding : AppRoute()
}
