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
        // Show password field only if email changed
        val oldEmail = SPreferenceManager.getUserEmail()
        binding.emailEditText.addTextChangedListener {
            binding.cardView.visibility =
                if (binding.emailEditText.text.toString().trim() != oldEmail) View.VISIBLE else View.GONE
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val newName = binding.userNameEditText.text.toString().trim()
            val newEmail = binding.emailEditText.text.toString().trim()
            val oldEmail = SPreferenceManager.getUserEmail()

            if (newName.isEmpty()) {
                binding.userNameEditText.error = "Name cannot be empty"
                return@setOnClickListener
            }

            if (newEmail.isEmpty()) {
                binding.emailEditText.error = "Email cannot be empty"
                return@setOnClickListener
            }

            if (newEmail != oldEmail) {
                val password = binding.currentPasswordEditText.text.toString().trim()
                if (password.isEmpty()) {
                    binding.currentPasswordEditText.error = "Enter current password"
                    return@setOnClickListener
                }

                // Pass both email and name for proper update
                viewModel.changeEmail(password, newEmail, newName)

            } else {
                // Only name changed
                viewModel.updateProfile(newName, newEmail)
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
                    findNavController().navigateUp() // Name-only update
                }
                is ProfileState.EmailVerificationSent -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Save Changes"
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    // Do NOT navigate; user must verify email
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
