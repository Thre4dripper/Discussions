package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Cloudinary
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.DiscussionModel
import com.example.discussions.repositories.DiscussionRepository
import com.example.discussions.repositories.PostRepository

class PostsViewModel : ViewModel() {

    private val TAG = "UserPostsViewModel"

    //all posts list from repository
    private var discussionsPostsList = DiscussionRepository.discussions

    //get user list directly from repository live data
    private var _userPostsList = PostRepository.userPostsList
    val userPostsList: LiveData<MutableList<DiscussionModel>?>
        get() = _userPostsList

    private var _isUserPostsFetched = MutableLiveData<String?>(null)
    val isUserPostsFetched: LiveData<String?>
        get() = _isUserPostsFetched

    private var _isPostDeleted = MutableLiveData<String?>(null)
    val isPostDeleted: LiveData<String?>
        get() = _isPostDeleted

    private var _isPostLikedChanged = MutableLiveData<String?>(null)
    val isPostLikedChanged: LiveData<String?>
        get() = _isPostLikedChanged

    companion object {
        var userPostsScrollToIndex = false
    }

    fun getAllUserPosts(context: Context, userId: String) {
        if (_isUserPostsFetched.value == Constants.API_SUCCESS) return
        else _isUserPostsFetched.value = null
        userPostsScrollToIndex = true

        PostRepository.getAllUserPosts(context, userId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isUserPostsFetched.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isUserPostsFetched.value = response
                _userPostsList.value = mutableListOf()
            }
        })
    }

    fun refreshUserPosts() {
        _isUserPostsFetched.value = null
    }

    fun deletePost(context: Context, postId: String) {
        _isPostDeleted.value = null
        userPostsScrollToIndex = false
        HomeViewModel.postsOrPollsOrNotificationsScrollToTop = false

        //deleted post for deleting image
        val deletedPost = discussionsPostsList.value?.find { it.post?.postId == postId }
            ?: userPostsList.value?.find { it.post?.postId == postId }!!

        PostRepository.deletePost(context, postId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPostDeleted.postValue(Constants.API_SUCCESS)

                val imageUrl =
                    deletedPost.post?.postImage ?: ""
                if (imageUrl.isNotEmpty()) Cloudinary.deleteImage(context, imageUrl)
            }

            override fun onError(response: String) {
                _isPostDeleted.postValue(Constants.API_FAILED)
            }
        })
    }

    fun likePost(context: Context, postId: String) {
        _isPostLikedChanged.value = null
        userPostsScrollToIndex = false
        HomeViewModel.postsOrPollsOrNotificationsScrollToTop = false

        PostRepository.likePost(context, postId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPostLikedChanged.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isPostLikedChanged.value = Constants.API_FAILED
            }
        })
    }
}