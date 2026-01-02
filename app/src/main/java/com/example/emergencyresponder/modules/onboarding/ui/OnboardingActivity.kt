package com.example.emergencyresponder.modules.onboarding.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.emergencyresponder.core.constants.AppConstant
import com.example.emergencyresponder.core.navigation.AppNavigator
import com.example.emergencyresponder.core.navigation.AppRoute
import com.example.emergencyresponder.core.objects.SPreferenceManager
import com.example.emergencyresponder.core.utils.AppPermissions
import com.example.emergencyresponder.databinding.ActivityOnboardingBinding
import com.example.emergencyresponder.modules.dashboard.ui.service.CrashDetectionService
import com.example.emergencyresponder.modules.onboarding.data.adapter.OnboardingViewPagerAdapter
import com.example.emergencyresponder.modules.onboarding.domain.viewmodel.OnboardingViewModel

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
            if (viewModel.isLastPage(currentPage, adapter.itemCount)) {
                if (AppPermissions.hasLocationPermission(this)) {
                    proceedAfterPermission()
                } else {
                    AppPermissions.requestLocationPermission(this, AppConstant.LOCATION_PERMISSION_CODE)
                }
            }else if (viewModel.startCrashDetection(currentPage, adapter.itemCount)){
                startCrashDetection()
                binding.onboardingViewPager.currentItem = currentPage + 1
            }else {
                binding.onboardingViewPager.currentItem = currentPage + 1
            }
        }

    }

    private fun startCrashDetection() {
            val intent = Intent(this, CrashDetectionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            viewModel.setCrashDetectionActive(true)
            Toast.makeText(this, "Emergency detection started", Toast.LENGTH_SHORT).show()

    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AppConstant.LOCATION_PERMISSION_CODE) {
            if (AppPermissions.hasLocationPermission(this)) {
                startCrashDetection()
                navigateToLogin()
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun navigateToLogin() {
        AppNavigator.navigate(
            activity = this,
            route = AppRoute.Login,
            finishCurrent = true
        )
    }
    private fun proceedAfterPermission() {
        SPreferenceManager.setOnboardingCompleted()
        AppNavigator.navigate(
            activity = this,
            route = AppRoute.Login,
            finishCurrent = true
        )
    }

}
