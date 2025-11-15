package com.example.emergencyresponder.modules.onboarding.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.emergencyresponder.modules.onboarding.ui.OnboardingActivity
import com.example.emergencyresponder.modules.onboarding.ui.OnboardingCrashDetectionFragment
import com.example.emergencyresponder.modules.onboarding.ui.OnboardingDriveSmarterFragment
import com.example.emergencyresponder.modules.onboarding.ui.OnboardingLocationFragment
import com.example.emergencyresponder.modules.onboarding.ui.OnboardingNotifyFragment

class ViewPagerAdapter (
    activity: OnboardingActivity
) : FragmentStateAdapter(activity){
    private  val fragments  = listOf(
        //SplashFragment(),
        OnboardingDriveSmarterFragment(),
        OnboardingCrashDetectionFragment(),
        OnboardingNotifyFragment(),
        OnboardingLocationFragment(),

    )

    override fun getItemCount(): Int = 4


    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}