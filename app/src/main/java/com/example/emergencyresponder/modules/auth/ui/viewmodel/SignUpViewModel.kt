package com.example.emergencyresponder.modules.auth.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencyresponder.R
import com.example.emergencyresponder.core.base.Event
import com.example.emergencyresponder.core.navigation.AppRoute
import com.example.emergencyresponder.core.utils.ValidationResult
import com.example.emergencyresponder.core.utils.Validator
import com.example.emergencyresponder.core.domain.model.EmergencyContact
import com.example.emergencyresponder.modules.auth.domain.model.User
import com.example.emergencyresponder.modules.auth.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {
    private val validator: Validator = Validator()

    private val _state = MutableLiveData<AuthUiState>()
    val state: LiveData<AuthUiState> = _state

    private val _route = MutableLiveData<Event<AppRoute>>()
    val route: LiveData<Event<AppRoute>> = _route
    val errors = MutableLiveData<Map<String, String?>>()
    private fun updateError(field: String, error: String?) {
        val currentMap = errors.value ?: emptyMap()
        errors.value = currentMap + (field to error)
    }
    fun onFieldFocusChanged(field: String, value: String, extra: String = "") {
        val result = when (field) {
            "email" -> validator.validateEmail(value)
            "userName", "contactName" -> validator.validateName(value)
            "phone" -> validator.validatePhone(value)
            "password" -> validator.validatePassword(value, extra)
            else -> ValidationResult(true)
        }
        updateError(field, if (result.isValid) null else result.errorMessage)
    }
    fun validateInput(
        userName: String,
        email: String,
        password: String,
        confirmPassword: String,
        name: String,
        phone: String
    ): Boolean {
        validator.validateName(userName).takeIf { !it.isValid }?.let {
            _state.value = AuthUiState.Error(it.errorMessage ?: "")
            return false
        }
        validator.validateEmail(email).takeIf { !it.isValid }?.let {
            _state.value = AuthUiState.Error(it.errorMessage ?: "")
            return false
        }
        validator.validatePassword(password, confirmPassword).takeIf { !it.isValid }?.let {
            _state.value = AuthUiState.Error(it.errorMessage ?: "")
            return false
        }
        validator.validateName(name).takeIf { !it.isValid }?.let {
            _state.value = AuthUiState.Error(it.errorMessage ?: "")
            return false
        }
        validator.validatePhone(phone).takeIf { !it.isValid }?.let {
            _state.value = AuthUiState.Error(it.errorMessage ?: "")
            return false
        }
        return true
    }

    fun signUp(userName: String, email: String, password: String, confirmPassword: String, emergencyPhone: String, contactName: String) {
        val isEmailValid = validator.validateEmail(email)
        val isNameValid = validator.validateName(userName)
        val isContactValid = validator.validateName(contactName)
        val isPhoneValid = validator.validatePhone(emergencyPhone)
        val isPasswordValid = validator.validatePassword(password, confirmPassword)

        if (!isEmailValid.isValid || !isNameValid.isValid || !isContactValid.isValid || !isPhoneValid.isValid || !isPasswordValid.isValid) {
            _state.value = AuthUiState.Error("Please fix the errors above")
            return
        }

        viewModelScope.launch {
            _state.value = AuthUiState.Loading
            try {
                val user = User(name = userName, email = email, emergencyContacts = listOf(EmergencyContact(contactName, emergencyPhone)))
                signUpUseCase(email, password, user)

                _state.value = AuthUiState.Success(R.string.account_created_verify_email)
                _route.value = Event(AppRoute.Login)
            } catch (e: Exception) {
                _state.value = AuthUiState.Error(e.message ?: "Signup failed")
            }
        }
    }
}

