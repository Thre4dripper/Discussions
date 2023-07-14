package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.ProfileDataModel
import com.example.discussions.repositories.ProfileRepository

class ProfileViewModel : ViewModel() {
    lateinit var profileDataModel: ProfileDataModel

    private var _isProfileFetched = MutableLiveData<String?>(null)
    val isProfileFetched: LiveData<String?>
        get() = _isProfileFetched

    fun getProfile(context: Context, username: String) {
        if (_isProfileFetched.value == Constants.API_SUCCESS) return
        else _isProfileFetched.value = null

        ProfileRepository.getProfile(context, username, object : ResponseCallback {
            override fun onSuccess(response: String) {
                profileDataModel = ProfileRepository.profileDataModel!!
                _isProfileFetched.postValue(Constants.API_SUCCESS)
            }

            override fun onError(response: String) {
                _isProfileFetched.postValue(response)
            }
        })
    }

    fun refreshProfile() {
        _isProfileFetched.value = null
    }
}