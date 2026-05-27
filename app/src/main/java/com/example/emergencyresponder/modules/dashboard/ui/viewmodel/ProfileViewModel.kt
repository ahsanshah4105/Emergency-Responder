package com.example.emergencyresponder.modules.dashboard.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencyresponder.core.base.Event
import com.example.emergencyresponder.core.common.PrefKeys
import com.example.emergencyresponder.core.domain.coroutines.DispatcherProvider
import com.example.emergencyresponder.core.domain.repository.IBasePreference
import com.example.emergencyresponder.core.domain.session.AuthSession
import com.example.emergencyresponder.core.navigation.AppRoute
import com.example.emergencyresponder.modules.dashboard.domain.repository.IProfileRepository
import com.example.emergencyresponder.modules.dashboard.domain.usecase.ChangeEmailUseCase
import com.example.emergencyresponder.modules.dashboard.domain.usecase.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val changeEmailUseCase: ChangeEmailUseCase,
    private val repository: IProfileRepository,
    private val prefProvider: IBasePreference,
    private val authSession: AuthSession,
    private val dispatchers: DispatcherProvider
) : ViewModel() {
    private val _navigationEvent = MutableLiveData<Event<AppRoute>>()
    val navigationEvent: LiveData<Event<AppRoute>> = _navigationEvent
    private val _state = MutableLiveData<Event<ProfileState>>(Event(ProfileState.Idle))
    val state: LiveData<Event<ProfileState>> = _state

    private val _userData = MutableLiveData<Pair<String, String>>()
    val userData: LiveData<Pair<String, String>> = _userData

    // Visibility State for UI
    private val _isReauthRequired = MutableLiveData<Boolean>(false)
    val isReauthRequired: LiveData<Boolean> = _isReauthRequired

    fun loadCurrentUserData() {
        val name = prefProvider.getString(PrefKeys.USER_NAME)
        val email = prefProvider.getString(PrefKeys.USER_EMAIL)
        _userData.value = name to email
    }

    fun onSaveClicked(newName: String, newEmail: String, currentPass: String, newPass: String) {
        val oldName = prefProvider.getString(PrefKeys.USER_NAME)
        val oldEmail = prefProvider.getString(PrefKeys.USER_EMAIL)
        val uid = prefProvider.getString(PrefKeys.USER_ID)

        executeAction {
            when {
                newPass.isNotEmpty() -> {
                    repository.updatePassword(currentPass, newPass)
                    ProfileState.Success(ProfileMessage.PASSWORD_UPDATED)
                }

                newEmail != oldEmail -> {
                    changeEmailUseCase(currentPass, newEmail, newName, uid)
                    ProfileState.EmailVerificationSent(ProfileMessage.VERIFY_EMAIL)
                }

                newName != oldName -> {
                    updateProfileUseCase(uid, newName, oldEmail)
                    prefProvider.saveString(PrefKeys.USER_NAME, newName)
                    loadCurrentUserData()
                    ProfileState.Success(ProfileMessage.SUCCESS)
                }
                else -> ProfileState.Idle
            }
        }
    }

    private fun executeAction(action: suspend () -> ProfileState) {
        viewModelScope.launch {
            _state.value = Event(ProfileState.Loading)
            try {
                _state.value = Event(withContext(dispatchers.io) { action() })
            } catch (e: Exception) {
                _state.value = Event(ProfileState.Error(ProfileMessage.GENERIC_ERROR, e.message))
            }
        }
    }

    fun resetState() { _state.value = Event(ProfileState.Idle) }

    fun logout() {
        prefProvider.clearUserSession()
        _navigationEvent.value = Event(AppRoute.Login)
    }

    fun handleVerificationSent() {
        logout()
    }
}

enum class ProfileMessage { SUCCESS, PASSWORD_UPDATED, VERIFY_EMAIL, GENERIC_ERROR }

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Success(val messageType: ProfileMessage) : ProfileState()
    data class Error(val messageType: ProfileMessage, val dynamicMsg: String? = null) : ProfileState()
    data class EmailVerificationSent(val messageType: ProfileMessage) : ProfileState()
}