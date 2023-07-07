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

    private val TAG = "PostsViewModel"

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
        var postsScrollToIndex = false

        /**
         * PAGINATION STUFF
         * only this has to be static because it is used in profile fragment and user posts activity
         */
        private var postsPage = 0
        val hasMorePosts = PostRepository.hasMorePosts
        val paginationStatus = MutableLiveData(Constants.PAGE_IDLE)
    }

    fun getAllUserPosts(context: Context, userId: String) {
        _isUserPostsFetched.value = null
        paginationStatus.value = Constants.PAGE_LOADING

        postsPage++
        PostRepository.getAllUserPosts(context, userId, postsPage, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isUserPostsFetched.value = Constants.API_SUCCESS
                paginationStatus.value = Constants.PAGE_IDLE
            }

            override fun onError(response: String) {
                _isUserPostsFetched.value = response
                _userPostsList.value = mutableListOf()
                paginationStatus.value = Constants.PAGE_IDLE
            }
        })
    }

    fun refreshUserPosts() {
        PostRepository.cancelGetRequest()
        PostRepository.userPostsList.value = null
        _isUserPostsFetched.value = null
        paginationStatus.value = Constants.PAGE_IDLE
        postsPage = 0
    }

    fun deletePost(context: Context, postId: String) {
        _isPostDeleted.value = null
        postsScrollToIndex = false

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
        postsScrollToIndex = false

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