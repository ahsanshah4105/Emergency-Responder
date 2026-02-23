package com.example.emergencyresponder.modules.dashboard.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencyresponder.core.manager.SPreferenceManager
import com.example.emergencyresponder.modules.auth.domain.usecase.ChangeEmailUseCase
import com.example.emergencyresponder.modules.auth.domain.usecase.UpdateProfileUseCase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ProfileViewModel(
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val changeEmailUseCase: ChangeEmailUseCase
) : ViewModel() {

    private val _state = MutableLiveData<ProfileState>(ProfileState.Idle)
    val state: LiveData<ProfileState> = _state

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> = _userEmail

    fun loadCurrentUserData() {
        _userName.value = SPreferenceManager.getUserName() ?: ""
        _userEmail.value = SPreferenceManager.getUserEmail() ?: ""
    }

    fun updatePassword(currentPassword: String, newPassword: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            _state.value = ProfileState.Error("User not logged in")
            return
        }

        viewModelScope.launch {
            _state.value = ProfileState.Loading
            try {
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                user.reauthenticate(credential).await()
                user.updatePassword(newPassword).await()
                _state.value = ProfileState.Success("Password updated successfully")
            } catch (e: Exception) {
                _state.value = ProfileState.Error(e.message ?: "Password update failed")
            }
        }
    }

    fun updateProfile(newName: String) {
        val uid = SPreferenceManager.getUserId()
        val email = SPreferenceManager.getUserEmail() ?: ""

        if (uid.isNullOrEmpty()) {
            _state.value = ProfileState.Error("User session not found. Please login again.")
            return
        }

        viewModelScope.launch {
            _state.value = ProfileState.Loading
            try {
                // Call the use case
                updateProfileUseCase(uid, newName, email)

                // Update local preferences
                SPreferenceManager.saveUserSession(
                    uid = uid,
                    name = newName,
                    email = email
                )

                // Update LiveData
                _userName.value = newName
                _state.value = ProfileState.Success("Profile updated successfully")

            } catch (e: Exception) {
                _state.value = ProfileState.Error(e.message ?: "Profile update failed")
            }
        }
    }

    fun changeEmail(password: String, newEmail: String, newName: String) {
        val auth = FirebaseAuth.getInstance()
        val currentEmail = auth.currentUser?.email ?: run {
            _state.value = ProfileState.Error("User not logged in")
            return
        }

        viewModelScope.launch {
            _state.value = ProfileState.Loading
            try {
                changeEmailUseCase(
                    currentEmail,
                    password,
                    newEmail,
                    newName
                )
                _state.value = ProfileState.EmailVerificationSent(
                    "Verification link sent to new email. Please verify to complete update."
                )
                auth.signOut()
                _state.value = ProfileState.Success("Email updated successfully")
            } catch (e: Exception) {
                _state.value = ProfileState.Error(e.message ?: "Email change failed")
            }
        }
    }

    fun resetState() {
        _state.value = ProfileState.Idle
    }
}

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Success(val message: String) : ProfileState()
    data class Error(val message: String) : ProfileState()
    data class EmailVerificationSent(val message: String) : ProfileState()
}
