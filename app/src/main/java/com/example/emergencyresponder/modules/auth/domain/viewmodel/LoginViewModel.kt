package com.example.emergencyresponder.modules.auth.domain.viewmodel

import androidx.compose.foundation.layout.Spacer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencyresponder.core.navigation.AppRoute
import com.example.emergencyresponder.core.objects.SPreferenceManager
import com.example.emergencyresponder.modules.auth.domain.useCase.LoginUseCase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableLiveData<AuthState>()
    val state: LiveData<AuthState> = _state

    private val _route = MutableLiveData<AppRoute>()
    val route: LiveData<AppRoute> = _route
    var firestore = FirebaseFirestore.getInstance()
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                loginUseCase(email, password)
                _state.value = AuthState.Success
                updateEmailInSessionAndFirestore(SPreferenceManager.getUserId(), email)
                _route.value = AppRoute.Dashboard
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }
    private fun updateEmailInSessionAndFirestore(uid: String?, email: String?) {
        val safeUid = uid ?: return              // Use uid from LoginViewModel or FirebaseAuth
        val safeEmail = email ?: return          // New email to update

        // 1️⃣ Update SharedPreferences
        SPreferenceManager.setUserEmail(safeEmail)

        // 2️⃣ Update Firestore
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users")
            .document(safeUid)
            .update("email", safeEmail)
            .addOnSuccessListener {
                // Optional: log success
                println("Firestore email updated successfully")
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                // Optional: show a Toast
                // Toast.makeText(context, "Failed to update email in Firestore", Toast.LENGTH_SHORT).show()
            }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                loginUseCase.executeGoogleLogin(idToken)
                _state.value = AuthState.Success
                _route.value = AppRoute.Dashboard
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Google Login failed")
            }
        }
    }

}

sealed class AuthState {
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
