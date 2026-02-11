package com.example.emergencyresponder.modules.dashboard.domain.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencyresponder.core.objects.SPreferenceManager
import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.auth.domain.useCase.UpdateProfileUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Success(val message: String) : ProfileState()
    data class Error(val message: String) : ProfileState()
}
class ProfileViewModel(
    private val updateProfileUseCase: UpdateProfileUseCase
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

    fun updateProfile(newName: String, newEmail: String) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            _state.value = ProfileState.Error("User not logged in")
            return
        }

        // Only proceed if email is verified
        if (!user.isEmailVerified) {
            _state.value = ProfileState.Error("Email not verified. Please check your inbox.")
            return
        }

        val uid = SPreferenceManager.getUserId()
        if (uid.isNullOrEmpty()) {
            _state.value = ProfileState.Error("User session not found. Please login again.")
            return
        }

        viewModelScope.launch {
            _state.value = ProfileState.Loading
            try {
                updateProfileUseCase(uid, newName, newEmail)

                // Save locally
                SPreferenceManager.saveUserSession(
                    uid = uid,
                    name = newName,
                    email = newEmail
                )

                _userName.value = newName
                _userEmail.value = newEmail
                _state.value = ProfileState.Success("Profile updated successfully")
            } catch (e: Exception) {
                _state.value = ProfileState.Error(e.message ?: "Update failed")
            }
        }
    }


    fun resetState() {
        _state.value = ProfileState.Idle
    }
}