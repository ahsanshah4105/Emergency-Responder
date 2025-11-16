package com.example.emergencyresponder.modules.onboarding.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.emergencyresponder.core.objects.SPreferenceManager
import com.example.emergencyresponder.databinding.ActivityOnboardingBinding
import com.example.emergencyresponder.modules.onboarding.adapter.OnboardingViewPagerAdapter
import com.example.emergencyresponder.navigation.OnboardingNavigator
import com.example.emergencyresponder.modules.onboarding.onboardingViewModel.OnboardingViewModel

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
                SPreferenceManager.setOnboardingCompleted()

                OnboardingNavigator.goToLogin(this)
            } else {
                binding.onboardingViewPager.currentItem = currentPage + 1
            }
        }
    }
}
