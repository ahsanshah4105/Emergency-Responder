package com.example.emergencyresponder.modules.auth.domain.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencyresponder.core.navigation.AppRoute
import com.example.emergencyresponder.core.utils.ValidationUtils
import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.model.User
import com.example.emergencyresponder.modules.auth.data.repository.SignUpRepositoryImpl
import com.example.emergencyresponder.modules.auth.domain.useCase.SignUpUseCase
import com.example.emergencyresponder.modules.auth.domain.useCase.SignUpValidator
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val validator: SignUpValidator = SignUpValidator(),
    private val signUpUseCase: SignUpUseCase
): ViewModel() {

    private val _state = MutableLiveData<AuthState>()
    val state: LiveData<AuthState> = _state

    private val _route = MutableLiveData<AppRoute>()
    val route: LiveData<AppRoute> = _route
    // ViewModel
    val emailError = MutableLiveData<String?>()
    val passwordError = MutableLiveData<String?>()
    val nameError = MutableLiveData<String?>()
    val phoneError = MutableLiveData<String?>()

    fun validateEmail(email: String) {
        emailError.value = if (ValidationUtils.isEmailValid(email)) null else "Invalid email"
    }

    fun validatePassword(password: String, confirmPassword: String) {
        passwordError.value = if (ValidationUtils.isPasswordMatch(password, confirmPassword)) null else "Passwords do not match"
    }

    fun validateName(name: String) {
        nameError.value = if (ValidationUtils.isNotEmpty(name)) null else "Name cannot be empty"
    }

    fun validatePhone(phone: String) {
        phoneError.value = if (ValidationUtils.isPhoneValid(phone)) null else "Invalid phone number"
    }

    // Validate input fields
    fun validateInput(
        email: String,
        password: String,
        confirmPassword: String,
        name: String,
        phone: String
    ): Boolean {
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
        email: String,
        password: String,
        confirmPassword: String,
        phone: String,
        emergencyName: String
    ) {
        viewModelScope.launch {
            try {
                val user = User(email, password, confirmPassword, phone, emergencyName)
                signUpUseCase(email, password, user)
                _state.value = AuthState.Success
                _route.value = AppRoute.Login
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Signup failed")
            }
        }
    }
}

