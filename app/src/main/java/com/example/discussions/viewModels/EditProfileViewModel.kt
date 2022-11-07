package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.api.ResponseCallback
import com.example.discussions.repositories.ProfileRepository

class EditProfileViewModel : ViewModel() {
    private val TAG = "EditProfileViewModel"

    var username: String = ""
    var firstName: String = ""
    var lastName: String = ""
    var gender: String = ""
    var email: String = ""
    var mobileNo: String = ""
    var dob: String = ""
    var address: String = ""

    private val _isProfileLoaded = MutableLiveData<Boolean>(null)
    private val _isProfileUpdated = MutableLiveData<Boolean>(null)

    val isProfileLoaded: LiveData<Boolean>
        get() = _isProfileLoaded

    val isProfileUpdated: LiveData<Boolean>
        get() = _isProfileUpdated

    fun getProfile(context: Context) {
        ProfileRepository.getProfile(context, object : ResponseCallback {
            override fun onSuccess(response: String) {
                ProfileRepository.map["username"]?.let { username = it }
                ProfileRepository.map["firstName"]?.let { firstName = it }
                ProfileRepository.map["lastName"]?.let { lastName = it }
                ProfileRepository.map["gender"]?.let { gender = it }
                ProfileRepository.map["email"]?.let { email = it }
                ProfileRepository.map["mobileNo"]?.let { mobileNo = it }
                ProfileRepository.map["dob"]?.let { dob = it }
                ProfileRepository.map["address"]?.let { address = it }
                _isProfileLoaded.value = true
            }

            override fun onError(response: String) {
                _isProfileLoaded.value = false
            }
        })

    }

    fun updateProfile(
        context: Context,
        imageUrl: String,
        firstName: String,
        lastName: String,
        gender: String,
        email: String,
        mobileNo: String,
        dob: String,
        address: String,
    ) {

        ProfileRepository.updateProfile(context,
            imageUrl,
            username,
            firstName,
            lastName,
            gender,
            email,
            mobileNo,
            dob,
            address,
            object : ResponseCallback {
                override fun onSuccess(response: String) {
                    _isProfileUpdated.value = true
                }

                override fun onError(response: String) {
                    _isProfileUpdated.value = false
                }
            })
    }
}