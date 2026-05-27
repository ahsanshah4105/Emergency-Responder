package com.example.emergencyresponder.modules.splash.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.emergencyresponder.R
import com.example.emergencyresponder.core.navigation.AppNavigator
import com.example.emergencyresponder.core.navigation.AppRoute
import com.example.emergencyresponder.modules.splash.ui.viewmodel.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        startAnimations()

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
