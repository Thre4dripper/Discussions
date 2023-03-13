package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.DiscussionModel
import com.example.discussions.models.ProfileDataModel
import com.example.discussions.repositories.PostRepository
import com.example.discussions.repositories.ProfileRepository

class ProfileViewModel : ViewModel() {
    lateinit var profileDataModel: ProfileDataModel

    private var _userPostsList = PostRepository.userPostsList
    val userPostsList: LiveData<MutableList<DiscussionModel>?>
        get() = _userPostsList

    private var _isProfileFetched = MutableLiveData<String?>(null)
    val isProfileFetched: LiveData<String?>
        get() = _isProfileFetched

    private var _isUserPostsFetched = MutableLiveData<String?>(null)
    val isUserPostsFetched: LiveData<String?>
        get() = _isUserPostsFetched

    fun getProfile(context: Context) {
        if (_isProfileFetched.value == Constants.API_SUCCESS) return
        else _isProfileFetched.value = null


        ProfileRepository.getProfile(context, object : ResponseCallback {
            override fun onSuccess(response: String) {
                profileDataModel = ProfileRepository.profileDataModel!!
                _isProfileFetched.postValue(Constants.API_SUCCESS)
            }

            override fun onError(response: String) {
                _isProfileFetched.postValue(response)
            }
        })
    }

    fun refreshProfile(context: Context) {
        _isProfileFetched = MutableLiveData<String?>(null)
        _isUserPostsFetched.value = null
        getAllUserPosts(context)
    }

    fun getAllUserPosts(context: Context) {
        PostRepository.getAllUserPosts(context, profileDataModel.userId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isUserPostsFetched.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isUserPostsFetched.value = response
                _userPostsList.value = mutableListOf()
            }
        })
    }
}