package com.example.emergencyresponder.modules.onboarding.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.emergencyresponder.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingDriveSmarterFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding_drive_smarter, container, false)
    }


}