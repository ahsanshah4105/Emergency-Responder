    package com.example.emergencyresponder.modules.dashboard.ui

    import android.content.Intent
    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Toast
    import androidx.core.view.isVisible
    import androidx.core.widget.addTextChangedListener
    import androidx.fragment.app.Fragment
    import androidx.fragment.app.viewModels
    import androidx.navigation.fragment.findNavController
    import com.example.emergencyresponder.R
    import com.example.emergencyresponder.databinding.FragmentEditProfileBinding
    import com.example.emergencyresponder.modules.auth.ui.LoginActivity
    import com.example.emergencyresponder.modules.dashboard.ui.viewmodel.ProfileMessage
    import com.example.emergencyresponder.modules.dashboard.ui.viewmodel.ProfileState
    import com.example.emergencyresponder.modules.dashboard.ui.viewmodel.ProfileViewModel
    import dagger.hilt.android.AndroidEntryPoint

    @AndroidEntryPoint
    class EditProfileFragment : Fragment() {

        private var _binding: FragmentEditProfileBinding? = null
        private val binding get() = _binding!!
        private val viewModel: ProfileViewModel by viewModels()

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            setupObservers()
            setupListeners()
            viewModel.loadCurrentUserData()
        }

        private fun setupObservers() {
            // 1. Data Observer
            viewModel.userData.observe(viewLifecycleOwner) { (name, email) ->
                binding.userNameEditText.setText(name)
                binding.emailEditText.setText(email)
                setupEmailWatcher(email) // Pass old email from here
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
            viewModel.state.observe(viewLifecycleOwner) { event ->
                event.getContentIfNotHandled()?.let { state ->
                    when (state) {
                        is ProfileState.Loading -> toggleLoading(true)
                        is ProfileState.Success -> {
                            toggleLoading(false)
                            showToast(mapEnumToString(state.messageType))
                            findNavController().navigateUp()
                        }
                        is ProfileState.Error -> {
                            toggleLoading(false)
                            showToast(state.dynamicMsg ?: mapEnumToString(state.messageType))
                        }
                        is ProfileState.EmailVerificationSent -> {
                            toggleLoading(false)
                            showToast(mapEnumToString(state.messageType))
                            viewModel.handleVerificationSent()
                        }
                        else -> toggleLoading(false)
                    }
                }
            }
        }

        private fun mapEnumToString(type: ProfileMessage): String {
            val resId = when(type) {
                ProfileMessage.SUCCESS -> R.string.profile_updated
                ProfileMessage.PASSWORD_UPDATED -> R.string.password_updated
                ProfileMessage.VERIFY_EMAIL -> R.string.verify_your_email
                ProfileMessage.GENERIC_ERROR -> R.string.err_generic
            }
            return getString(resId)
        }

        private fun setupEmailWatcher(oldEmail: String) {
            binding.emailEditText.addTextChangedListener {
                val hasEmailChanged = binding.emailEditText.text.toString().trim() != oldEmail
                binding.cardView.isVisible = hasEmailChanged || binding.cardVieww.isVisible
            }
        }

        private fun toggleLoading(isLoading: Boolean) {
            binding.progressBar.isVisible = isLoading
            binding.btnSave.isEnabled = !isLoading
        }

        private fun showToast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        private fun setupListeners() {
            binding.changePassword.setOnClickListener {
                if (binding.cardVieww.isVisible) {
                    binding.cardVieww.visibility = View.GONE
                } else {

                    binding.cardVieww.visibility = View.VISIBLE
                }
            }

            binding.btnSave.setOnClickListener {
                viewModel.onSaveClicked(
                    newName = binding.userNameEditText.text.toString(),
                    newEmail = binding.emailEditText.text.toString(),
                    currentPass = binding.currentPasswordEditText.text.toString(),
                    newPass = binding.newPasswordEditText.text.toString()
                )
            }

        }
        private fun navigateToLogin() {
            val intent = Intent(requireActivity(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }
        override fun onDestroyView() {
            super.onDestroyView()
            viewModel.resetState()
            _binding = null
        }
    }
