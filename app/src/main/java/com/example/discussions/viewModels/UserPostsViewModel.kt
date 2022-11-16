package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.PostModel
import com.example.discussions.repositories.PostRepository

class UserPostsViewModel : ViewModel() {

    private val TAG = "UserPostsViewModel"

    private var _userPosts = MutableLiveData<List<PostModel>>()
    val userPosts: LiveData<List<PostModel>>
        get() = _userPosts

    fun getUserPosts() {
        _userPosts.value = PostRepository.userPostsList
    }

    private var _isPostDeleted = MutableLiveData<String?>(null)
    val isPostDeleted: LiveData<String?>
        get() = _isPostDeleted

    fun deletePost(context: Context, postId: String) {
        _isPostDeleted.value = null

        PostRepository.deletePost(context, postId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPostDeleted.postValue(Constants.API_SUCCESS)

                //re-adding post when error occurs
                val newUserList = _userPosts.value!!.toMutableList()
                val post = _userPosts.value!!.find { it.postId == postId }
                newUserList.remove(post)
                _userPosts.postValue(newUserList)
            }

            override fun onError(response: String) {
                _isPostDeleted.postValue(Constants.API_FAILED)
            }
        })
    }
}