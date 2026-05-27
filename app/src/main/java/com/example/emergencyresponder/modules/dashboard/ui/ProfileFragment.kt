package com.example.emergencyresponder.modules.dashboard.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.emergencyresponder.R
import com.example.emergencyresponder.databinding.FragmentProfileBinding
import com.example.emergencyresponder.modules.dashboard.ui.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.userData.observe(viewLifecycleOwner) { (name, email) ->
            binding.tvUserName.text = name
            binding.tvUserEmail.text = email
        }

        setupObservers()
        setupListeners()
        viewModel.loadCurrentUserData()
    }
    private fun setupObservers() {
        viewModel.userData.observe(viewLifecycleOwner) { (name, email) ->
            binding.tvUserName.text = name
            binding.tvUserEmail.text = email
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { route ->
                com.example.emergencyresponder.core.navigation.AppNavigator.navigate(
                    context = requireContext(),
                    route = route,
                    finishCurrent = true
                )
            }
        }
    }
    private fun setupListeners() {
        binding.logoutBtn.setOnClickListener {
            viewModel.logout()
        }

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment2_to_editProfileFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}