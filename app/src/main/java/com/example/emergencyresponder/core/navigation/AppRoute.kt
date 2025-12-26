package com.example.emergencyresponder.core.navigation

sealed class AppRoute {
    object Login : AppRoute()
    object SignUp : AppRoute()
    object Dashboard : AppRoute()
}
