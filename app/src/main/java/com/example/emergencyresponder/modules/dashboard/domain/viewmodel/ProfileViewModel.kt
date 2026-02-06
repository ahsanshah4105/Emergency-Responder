package com.example.emergencyresponder.modules.dashboard.domain.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencyresponder.core.objects.SPreferenceManager
import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    // ✅ FIXED: Use UserRemoteDataSource (Firestore) and create an instance ()
    private val repository = UserRemoteDataSource()

    private val _updateStatus = MutableLiveData<Result<String>>()
    val updateStatus: LiveData<Result<String>> get() = _updateStatus

//    fun updateProfile(newName: String, newPhone: String) {
//        val uid = SPreferenceManager.getUserId()
//        val email = SPreferenceManager.getUserEmail()
//
//        if (uid.isNullOrEmpty()) {
//            _updateStatus.value = Result.failure(Exception("User not logged in"))
//            return
//        }
//
//        viewModelScope.launch {
//            // ✅ FIXED: Calling the function on the instance
//            repository.updateUserProfile(uid, newName, newPhone) { success, errorMessage ->
//                if (success) {
//                    SPreferenceManager.saveUserSession(
//                        uid = uid,
//                        name = newName,
//                        email = email ?: "",
//                        phone = newPhone
//                    )
//                    _updateStatus.value = Result.success("Profile Updated Successfully")
//                } else {
//                    _updateStatus.value = Result.failure(Exception(errorMessage))
//                }
//            }
//        }
//    }
}