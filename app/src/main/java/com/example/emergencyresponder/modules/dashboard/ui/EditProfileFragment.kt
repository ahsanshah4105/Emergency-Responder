package com.example.emergencyresponder.modules.dashboard.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.emergencyresponder.core.objects.SPreferenceManager
import com.example.emergencyresponder.databinding.FragmentEditProfileBinding
import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.repository.ProfileRepositoryImpl
import com.example.emergencyresponder.modules.auth.domain.useCase.ChangeEmailUseCase
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
        setupObservers()
        setupListeners()
        setupEmailWatcher()
    }

    private fun setupViewModel() {
        val userDataSource = UserRemoteDataSource()
        val authDataSource = AuthRemoteDataSource()
        val repository = ProfileRepositoryImpl(userDataSource, authDataSource)
        val updateProfileUseCase = UpdateProfileUseCase(repository)
        val changeEmailUseCase = ChangeEmailUseCase(repository)
        val factory = ProfileViewModelFactory(updateProfileUseCase, changeEmailUseCase)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]
    }

    private fun populateCurrentData() {
        binding.userNameEditText.setText(SPreferenceManager.getUserName())
        binding.emailEditText.setText(SPreferenceManager.getUserEmail())
    }

    private fun setupEmailWatcher() {
        val oldEmail = SPreferenceManager.getUserEmail()
        binding.emailEditText.addTextChangedListener {
            // Show current password field only if email changed or password change requested
            binding.cardView.visibility =
                if (binding.emailEditText.text.toString().trim() != oldEmail
                    || binding.newPasswordEditText.visibility == View.VISIBLE
                ) View.VISIBLE else View.GONE
        }
    }

    private fun setupListeners() {
        // Optional: Toggle password field visibility if user wants to change password
        binding.changePassword.setOnClickListener {
            if (binding.cardVieww.visibility == View.VISIBLE) {
                binding.cardVieww.visibility = View.GONE
            } else {

                binding.cardVieww.visibility = View.VISIBLE
            }
        }

        binding.btnSave.setOnClickListener {
            val newName = binding.userNameEditText.text.toString().trim()
            val newEmail = binding.emailEditText.text.toString().trim()
            val oldEmail = SPreferenceManager.getUserEmail()
            val currentPassword = binding.currentPasswordEditText.text.toString().trim()
            val newPassword = binding.newPasswordEditText.text.toString().trim()

            var isAnyAction = false

            // --- NAME UPDATE ---
            if (newName.isNotEmpty() && newName != SPreferenceManager.getUserName()) {
                isAnyAction = true
                viewModel.updateProfile(newName)
            }

            // --- PASSWORD UPDATE ---
            if (binding.newPasswordEditText.visibility == View.VISIBLE && newPassword.isNotEmpty()) {
                if (currentPassword.isEmpty()) {
                    binding.currentPasswordEditText.error = "Enter current password to change password"
                    return@setOnClickListener
                }
                isAnyAction = true
                viewModel.updatePassword(currentPassword, newPassword)
            }

            if (newEmail != oldEmail) {
                if (currentPassword.isEmpty()) {
                    binding.currentPasswordEditText.error = "Enter current password to change email"
                    return@setOnClickListener
                }
                isAnyAction = true
                viewModel.changeEmail(currentPassword, newEmail, newName)
            }

            if (!isAnyAction) {
                Toast.makeText(requireContext(), "No changes detected", Toast.LENGTH_SHORT).show()
            }
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
                    binding.btnSave.text = "Update Profile"
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is ProfileState.EmailVerificationSent -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Save Changes"
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
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
        viewModel.resetState()
        _binding = null
    }
}
