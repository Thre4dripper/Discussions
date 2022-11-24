package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.ProfileDataModel
import com.example.discussions.repositories.PollRepository
import com.example.discussions.repositories.PostRepository
import com.example.discussions.repositories.UserRepository

class HomeViewModel : ViewModel() {
    private val TAG = "HomeViewModel"

    lateinit var profileDataModel: ProfileDataModel

    //get posts list directly from repository live data
    var postsList = PostRepository.allPostsList

    //get user posts list directly from repository live data
    var userPostsList = PostRepository.userPostsList

    //get user polls list directly from repository live data
    var userPollsList = PollRepository.userPollsList

    private var _isPostsFetched = MutableLiveData<String?>(null)
    val isPostsFetched: LiveData<String?>
        get() = _isPostsFetched

    private var _isProfileFetched = MutableLiveData<String?>(null)
    val isProfileFetched: LiveData<String?>
        get() = _isProfileFetched

    private var _isUserPostsFetched = MutableLiveData<String?>(null)
    val isUserPostsFetched: LiveData<String?>
        get() = _isUserPostsFetched

    private var _isUserPollsFetched = MutableLiveData<String?>(null)
    val isUserPollsFetched: LiveData<String?>
        get() = _isUserPollsFetched


    fun getProfile(context: Context) {
        if (_isProfileFetched.value == Constants.API_SUCCESS)
            return
        else
            _isProfileFetched.value = null


        UserRepository.getProfile(context, object : ResponseCallback {
            override fun onSuccess(response: String) {
                profileDataModel = UserRepository.profileDataModel!!
                _isProfileFetched.postValue(Constants.API_SUCCESS)
            }

            override fun onError(response: String) {
                _isProfileFetched.postValue(response)
            }
        })
    }

    fun getAllUserPosts(context: Context) {
        PostRepository.getAllUserPosts(context, profileDataModel.userId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isUserPostsFetched.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isUserPostsFetched.value = response
                userPostsList.value = mutableListOf()
            }
        })
    }

    fun refreshProfile(context: Context) {
        _isProfileFetched = MutableLiveData<String?>(null)
        _isUserPostsFetched.value = null
        userPostsList.value = null
        getAllUserPosts(context)
    }

    fun getAllPosts(context: Context) {
        if (_isPostsFetched.value == Constants.API_SUCCESS)
            return
        else {
            postsList.value = null
            _isPostsFetched.value = null
        }

        PostRepository.getAllPosts(context, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPostsFetched.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isPostsFetched.value = response
                postsList.value = mutableListOf()
            }
        })
    }

    fun refreshAllPosts(context: Context) {
        postsList.value = null
        _isPostsFetched.value = null
        getAllPosts(context)
    }

    fun getAllUserPolls(context: Context) {
        if (_isUserPollsFetched.value == Constants.API_SUCCESS)
            return
        else {
            userPollsList.value = null
            _isUserPollsFetched.value = null
        }

        PollRepository.getAllUserPolls(context, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isUserPollsFetched.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isUserPollsFetched.value = response
                userPollsList.value = mutableListOf()
            }
        })
    }

    fun refreshAllUserPolls(context: Context) {
        userPollsList.value = null
        _isUserPollsFetched.value = null
        getAllUserPolls(context)
    }
}