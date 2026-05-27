package com.example.emergencyresponder.modules.onboarding.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.emergencyresponder.R
import com.example.emergencyresponder.core.common.AppConstant
import com.example.emergencyresponder.core.navigation.AppNavigator
import com.example.emergencyresponder.core.navigation.AppRoute
import com.example.emergencyresponder.core.utils.AppPermissions
import com.example.emergencyresponder.databinding.ActivityOnboardingBinding
import com.example.emergencyresponder.modules.onboarding.ui.adapter.OnboardingViewPagerAdapter
import com.example.emergencyresponder.modules.onboarding.ui.viewmodel.OnboardingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding

    private val viewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupObservers()
        setupListeners()
    }

    private fun setupViewPager() {
        val adapter = OnboardingViewPagerAdapter(this)
        binding.onboardingViewPager.adapter = adapter
        binding.dotsIndicator.attachTo(binding.onboardingViewPager)
    }

    private fun setupObservers() {
        viewModel.navigationEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { navigation ->
                when (navigation) {
                    is OnboardingViewModel.OnboardingNavigation.NavigateToLogin -> {
                        AppNavigator.navigate(this, AppRoute.Login, finishCurrent = true)
                    }

                    is OnboardingViewModel.OnboardingNavigation.RequestPermission -> {
                        AppPermissions.requestLocationPermission(
                            this,
                            AppConstant.LOCATION_PERMISSION_CODE
                        )
                    }

                    is OnboardingViewModel.OnboardingNavigation.MoveToNextPage -> {
                        binding.onboardingViewPager.currentItem = navigation.nextIndex
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.continueBtn.setOnClickListener {
            viewModel.onContinueClicked(
                currentPage = binding.onboardingViewPager.currentItem,
                totalItems = binding.onboardingViewPager.adapter?.itemCount ?: 0,
                hasPermission = AppPermissions.hasLocationPermission(this)
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AppConstant.LOCATION_PERMISSION_CODE) {
            if (AppPermissions.hasLocationPermission(this)) {
                viewModel.onContinueClicked(
                    binding.onboardingViewPager.currentItem,
                    binding.onboardingViewPager.adapter?.itemCount ?: 0,
                    true
                )
            } else {
                // Show a simple feedback; actual localized message is in strings.xml
                android.widget.Toast.makeText(this, R.string.location_permission_required, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

}
