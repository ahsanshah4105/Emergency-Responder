package com.example.emergencyresponder.modules.onboarding.data.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.emergencyresponder.modules.onboarding.ui.OnboardingActivity
import com.example.emergencyresponder.modules.onboarding.ui.OnboardingCrashDetectionFragment
import com.example.emergencyresponder.modules.onboarding.ui.OnboardingDriveSmarterFragment
import com.example.emergencyresponder.modules.onboarding.ui.OnboardingLocationFragment
import com.example.emergencyresponder.modules.onboarding.ui.OnboardingNotifyFragment


class OnboardingViewPagerAdapter(activity: OnboardingActivity) :
    FragmentStateAdapter(activity) {

    private val fragments = listOf(
        OnboardingDriveSmarterFragment(),
        OnboardingNotifyFragment(),
        OnboardingCrashDetectionFragment(),
        OnboardingLocationFragment()
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}
