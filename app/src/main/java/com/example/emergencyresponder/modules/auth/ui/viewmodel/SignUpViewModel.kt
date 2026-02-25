package com.example.emergencyresponder.modules.auth.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencyresponder.core.base.Event
import com.example.emergencyresponder.core.navigation.AppRoute
import com.example.emergencyresponder.core.utils.ValidationResult
import com.example.emergencyresponder.core.utils.ValidationUtils
import com.example.emergencyresponder.modules.auth.data.model.EmergencyContact
import com.example.emergencyresponder.modules.auth.data.model.User
import com.example.emergencyresponder.modules.auth.domain.usecase.SignUpUseCase
import com.example.emergencyresponder.core.utils.Validator
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val signUpUseCase: SignUpUseCase
): ViewModel() {
    private val validator: Validator = Validator()

    private val _state = MutableLiveData<AuthState>()
    val state: LiveData<AuthState> = _state

    private val _route = MutableLiveData<Event<AppRoute>>()
    val route: LiveData<Event<AppRoute>> = _route
    val userNameError = MutableLiveData<String?>()

    val emailError = MutableLiveData<String?>()
    val passwordError = MutableLiveData<String?>()
    val nameError = MutableLiveData<String?>()

    private  val _phoneError = MutableLiveData<String?>()
    val phoneError: LiveData<String?> = _phoneError
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
    fun validateUserName(name: String) {
        emailError.value = if (ValidationUtils.isNotEmpty(name)) null else "Name cannot be empty"
    }
    fun validateEmail(email: String) {
        emailError.value = if (ValidationUtils.isEmailValid(email)) null else "Invalid email"
    }

    fun validateName(name: String) {
        nameError.value = if (ValidationUtils.isNotEmpty(name)) null else "Name cannot be empty"
    }

    fun validatePhone(phone: String) {
        val isValid = ValidationUtils.isPhoneValid(phone, "PK") // Default to Pakistan or US

        if (phone.isEmpty()) {
            _phoneError.value = "Phone number is required"
        } else if (!isValid) {
            _phoneError.value = "Invalid phone format"
        } else {
            _phoneError.value = null // No error
        }
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
            _state.value = AuthState.Error(it.errorMessage ?: "")
            return false
        }
        validator.validateEmail(email).takeIf { !it.isValid }?.let {
            _state.value = AuthState.Error(it.errorMessage ?: "")
            return false
        }
        validator.validatePassword(password, confirmPassword).takeIf { !it.isValid }?.let {
            _state.value = AuthState.Error(it.errorMessage ?: "")
            return false
        }
        validator.validateName(name).takeIf { !it.isValid }?.let {
            _state.value = AuthState.Error(it.errorMessage ?: "")
            return false
        }
        validator.validatePhone(phone).takeIf { !it.isValid }?.let {
            _state.value = AuthState.Error(it.errorMessage ?: "")
            return false
        }
        return true
    }

    fun signUp(userName: String, email: String, password: String, confirmPassword: String, emergencyPhone: String, contactName: String) {
        // Full validation check before proceeding
        val isEmailValid = validator.validateEmail(email)
        val isNameValid = validator.validateName(userName)
        val isContactValid = validator.validateName(contactName)
        val isPhoneValid = validator.validatePhone(emergencyPhone)
        val isPasswordValid = validator.validatePassword(password, confirmPassword)

        if (!isEmailValid.isValid || !isNameValid.isValid || !isContactValid.isValid || !isPhoneValid.isValid || !isPasswordValid.isValid) {
            _state.value = AuthState.Error("Please fix the errors above")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val user = User(name = userName, email = email, emergencyContacts = listOf(EmergencyContact(contactName, emergencyPhone)))
                signUpUseCase(email, password, user)

                // Fixed Error #5: Better success message
                _state.value = AuthState.Success("Account created! Please verify your email.")
                _route.value = Event(AppRoute.Login)
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Signup failed")
            }
        }
    }
}

