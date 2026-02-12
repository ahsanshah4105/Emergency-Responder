package com.example.emergencyresponder.modules.dashboard.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.emergencyresponder.R
import com.example.emergencyresponder.core.objects.SPreferenceManager
import com.example.emergencyresponder.databinding.FragmentProfileBinding
import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.repository.ProfileRepositoryImpl
import com.example.emergencyresponder.modules.auth.domain.useCase.ChangeEmailUseCase
import com.example.emergencyresponder.modules.auth.domain.useCase.UpdateProfileUseCase
import com.example.emergencyresponder.modules.auth.ui.LoginActivity
import com.example.emergencyresponder.modules.dashboard.domain.viewModelFactory.ProfileViewModelFactory
import com.example.emergencyresponder.modules.dashboard.domain.viewmodel.ProfileState
import com.example.emergencyresponder.modules.dashboard.domain.viewmodel.ProfileViewModel


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val dataSource = UserRemoteDataSource()
        val authDataSource = AuthRemoteDataSource()
        val repository = ProfileRepositoryImpl(dataSource, authDataSource)
        val updateProfileUseCase = UpdateProfileUseCase(repository)
        val changeEmailUseCase = ChangeEmailUseCase(repository)
        val factory = ProfileViewModelFactory(updateProfileUseCase, changeEmailUseCase )
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]

        setupObservers()
        setupListeners()

        // Load initial data
        viewModel.loadCurrentUserData()
    }

    private fun setupObservers() {
        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvUserName.text = name
        }
        viewModel.userEmail.observe(viewLifecycleOwner) { email ->
            binding.tvUserEmail.text = email
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileState.Loading -> {
                    // Show progress bar if you have one
                }
                is ProfileState.Success -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is ProfileState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun setupListeners() {
        binding.logoutBtn.setOnClickListener {
            SPreferenceManager.logoutUser()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
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