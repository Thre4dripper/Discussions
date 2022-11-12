package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.PostModel
import com.example.discussions.models.ProfileDataModel
import com.example.discussions.repositories.PostRepository
import com.example.discussions.repositories.UserRepository

class HomeViewModel : ViewModel() {
    private val TAG = "HomeViewModel"

    lateinit var profileDataModel: ProfileDataModel
    var postsList = MutableLiveData<MutableList<PostModel>?>()

    private val _isApiFetched = MutableLiveData<String?>(null)
    val isApiFetched: LiveData<String?>
        get() = _isApiFetched


    fun getProfile(context: Context) {
        _isApiFetched.value = null
        UserRepository.getProfile(context, object : ResponseCallback {
            override fun onSuccess(response: String) {
                profileDataModel = UserRepository.profileDataModel!!
                _isApiFetched.postValue(Constants.API_SUCCESS)
            }

            override fun onError(response: String) {
                _isApiFetched.postValue(response)
            }
        })
    }

    fun getAllPosts(context: Context) {
        PostRepository.getAllPosts(context, object : ResponseCallback {
            override fun onSuccess(response: String) {
                postsList.postValue(PostRepository.postsList)
                _isApiFetched.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isApiFetched.value = response
                postsList.value = mutableListOf()
            }
        })
    }

    fun refreshPosts(context: Context) {
        postsList.value = null
        _isApiFetched.value = null
        getAllPosts(context)
    }
}