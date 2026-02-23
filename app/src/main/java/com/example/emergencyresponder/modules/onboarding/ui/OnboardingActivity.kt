package com.example.emergencyresponder.modules.onboarding.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.emergencyresponder.core.constants.AppConstant
import com.example.emergencyresponder.core.navigation.AppNavigator
import com.example.emergencyresponder.core.navigation.AppRoute
import com.example.emergencyresponder.core.manager.SPreferenceManager
import com.example.emergencyresponder.core.utils.AppPermissions
import com.example.emergencyresponder.databinding.ActivityOnboardingBinding
import com.example.emergencyresponder.modules.onboarding.ui.adapter.OnboardingViewPagerAdapter
import com.example.emergencyresponder.modules.onboarding.ui.viewmodel.OnboardingViewModel

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private val viewModel: OnboardingViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupListeners()

    }

    private fun setupViewPager() {
        val adapter = OnboardingViewPagerAdapter(this)
        binding.onboardingViewPager.adapter = adapter
        binding.dotsIndicator.attachTo(binding.onboardingViewPager)
    }

    private fun setupListeners() {
        val adapter = binding.onboardingViewPager.adapter!!

        binding.continueBtn.setOnClickListener {
            val currentPage = binding.onboardingViewPager.currentItem
            val adapter = binding.onboardingViewPager.adapter!!

            if (viewModel.isLastPage(currentPage, adapter.itemCount)) {
                if (AppPermissions.hasLocationPermission(this)) {
                    proceedAfterPermission()
                } else {
                    AppPermissions.requestLocationPermission(
                        this,
                        AppConstant.LOCATION_PERMISSION_CODE
                    )
                }
            } else {
                binding.onboardingViewPager.currentItem = currentPage + 1
            }
        }


    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AppConstant.LOCATION_PERMISSION_CODE) {
            if (AppPermissions.hasLocationPermission(this)) {
                navigateToLogin()
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun navigateToLogin() {
        AppNavigator.navigate(
            context = this,
            route = AppRoute.Login,
            finishCurrent = true
        )
    }
    private fun proceedAfterPermission() {
        SPreferenceManager.setOnboardingCompleted()
        AppNavigator.navigate(
            context = this,
            route = AppRoute.Login,
            finishCurrent = true
        )
    }

}
