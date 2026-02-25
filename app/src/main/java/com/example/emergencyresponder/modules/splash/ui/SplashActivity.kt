package com.example.emergencyresponder.modules.splash.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.emergencyresponder.core.navigation.AppNavigator
import com.example.emergencyresponder.core.navigation.AppRoute
import com.example.emergencyresponder.modules.splash.ui.viewmodel.SplashViewModel
import android.widget.ImageView
import android.widget.LinearLayout
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import com.example.emergencyresponder.R
import com.example.emergencyresponder.core.base.EmergencyResponderApp
import com.example.emergencyresponder.modules.splash.ui.viewmodelfactory.SplashViewModelFactory

class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels {
        val container = (application as EmergencyResponderApp).appContainer
        SplashViewModelFactory(container.splashRepository) // Pure Interface Injection
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        startAnimations()

        // 3 seconds delay for branding, but logic is handled by VM
        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.checkDestination()
        }, 3000)

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.navigationEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { navigation ->
                when (navigation) {
                    is SplashViewModel.SplashNavigation.Dashboard -> AppNavigator.navigate(
                        this,
                        AppRoute.Dashboard
                    )

                    is SplashViewModel.SplashNavigation.Login -> AppNavigator.navigate(
                        this,
                        AppRoute.Login
                    )

                    is SplashViewModel.SplashNavigation.Onboarding -> AppNavigator.navigate(
                        this,
                        AppRoute.Onboarding
                    )
                }
                finish()
            }
        }
    }

    private fun startAnimations() {
        val animLeft = AnimationUtils.loadAnimation(this, R.anim.from_left)
        val animRight = AnimationUtils.loadAnimation(this, R.anim.from_right)
        findViewById<ImageView>(R.id.imageView2).startAnimation(animLeft)
        findViewById<LinearLayout>(R.id.textLayout).startAnimation(animRight)
    }
}
