package com.example.emergencyresponder.modules.auth.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencyresponder.core.navigation.AppRoute
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

    private val _route = MutableLiveData<AppRoute>()
    val route: LiveData<AppRoute> = _route
    val userNameError = MutableLiveData<String?>()

    val emailError = MutableLiveData<String?>()
    val passwordError = MutableLiveData<String?>()
    val nameError = MutableLiveData<String?>()

    private  val _phoneError = MutableLiveData<String?>()
    val phoneError: LiveData<String?> = _phoneError



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

    fun signUp(
        userName: String,
        email: String,
        password: String,
        confirmPassword: String,
        emergencyPhone: String,
        emergencyName: String
    ) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {

                val primaryContact = EmergencyContact(
                    name = emergencyName,
                    phone = emergencyPhone
                )
                val user = User(
                    uid = "",
                    name = userName,
                    email = email,
                    emergencyContacts = listOf(primaryContact)
                )
                signUpUseCase(email, password, user)
                _state.value = AuthState.Success
                _route.value = AppRoute.Login
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Signup failed")
            }
        }
    }
}

