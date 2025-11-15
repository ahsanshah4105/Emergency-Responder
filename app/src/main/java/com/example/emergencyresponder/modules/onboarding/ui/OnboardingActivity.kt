package com.example.emergencyresponder.modules.onboarding.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.emergencyresponder.R
import com.example.emergencyresponder.modules.onboarding.adapter.ViewPagerAdapter
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator

class OnboardingActivity : AppCompatActivity() {

    lateinit var viewPager : ViewPager2
    lateinit var dots: WormDotsIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        initializeViews()
        addingPageIndicators()
    }

    private fun addingPageIndicators() {
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        dots.attachTo(viewPager)
    }



    private fun initializeViews() {
        viewPager = findViewById(R.id.onboardingViewPager)
        dots = findViewById(R.id.dotsIndicator)
    }


}