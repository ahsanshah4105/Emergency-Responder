package com.example.emergencyresponder.modules.dashboard.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Toast

import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.emergencyresponder.core.objects.SPreferenceManager
import com.example.emergencyresponder.databinding.FragmentEditProfileBinding
import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.repository.ProfileRepositoryImpl
import com.example.emergencyresponder.modules.auth.domain.useCase.UpdateProfileUseCase

import com.example.emergencyresponder.modules.dashboard.domain.viewModelFactory.ProfileViewModelFactory
import com.example.emergencyresponder.modules.dashboard.domain.viewmodel.ProfileState
import com.example.emergencyresponder.modules.dashboard.domain.viewmodel.ProfileViewModel

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        populateCurrentData()
        setupListeners()
        setupObservers()
    }

    private fun setupViewModel() {
        // Manual DI (Same as ProfileFragment to share the logic)
        val dataSource = UserRemoteDataSource()
        val repository = ProfileRepositoryImpl(dataSource)
        val useCase = UpdateProfileUseCase(repository)
        val factory = ProfileViewModelFactory(useCase)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]
    }

    private fun populateCurrentData() {
        // Pre-fill fields from SharedPreferences or ViewModel
        binding.userNameEditText.setText(SPreferenceManager.getUserName())
        binding.emailEditText.setText(SPreferenceManager.getUserEmail())

        // If you have phone saved, set it here:
        // binding.etPhone.setText(SPreferenceManager.getUserPhone())
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSave.setOnClickListener {
            val newName = binding.userNameEditText.text.toString().trim()
            val newEmail = binding.emailEditText.text.toString().trim()

            if (newName.isEmpty()) {
                binding.userNameEditText.error = "Name cannot be empty"
                return@setOnClickListener
            }

            viewModel.updateProfile(newName, newEmail)
        }
    }

    private fun setupObservers() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                    binding.btnSave.text = "Saving..."
                }
                is ProfileState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Save Changes"
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()

                    // Navigate back to Profile Screen
                    findNavController().navigateUp()
                }
                is ProfileState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Save Changes"
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Reset state so it doesn't auto-trigger when coming back
        viewModel.resetState()
        _binding = null
    }
}