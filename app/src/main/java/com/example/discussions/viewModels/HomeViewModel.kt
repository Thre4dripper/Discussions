package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.ProfileDataModel
import com.example.discussions.repositories.UserRepository

class HomeViewModel : ViewModel() {
    companion object {
        const val API_SUCCESS = "success"
    }

    lateinit var profileDataModel: ProfileDataModel


    private val _isProfileLoaded = MutableLiveData<String>(null)
    val isProfileLoaded: LiveData<String>
        get() = _isProfileLoaded


    fun getProfile(context: Context) {
        UserRepository.getProfile(context, object : ResponseCallback {
            override fun onSuccess(response: String) {
                profileDataModel = UserRepository.profileDataModel!!
                _isProfileLoaded.postValue(API_SUCCESS)
            }

            override fun onError(response: String) {
                _isProfileLoaded.postValue(response)
            }

        })
    }
}