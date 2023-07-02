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

    /**
     * PAGINATION STUFF
     */
    private var postsPage = 0
    val hasMorePosts = PostRepository.hasMorePosts

    private var _isLoadingMore = MutableLiveData(Constants.PAGE_IDLE)
    val isLoadingMore: LiveData<String?>
        get() = _isLoadingMore

    companion object {
        var userPostsScrollToIndex = false
    }

    fun getAllUserPosts(context: Context, userId: String) {
        _isUserPostsFetched.value = null
        _isLoadingMore.value = Constants.PAGE_LOADING

        postsPage++
        PostRepository.getAllUserPosts(context, userId, postsPage, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isUserPostsFetched.value = Constants.API_SUCCESS
                _isLoadingMore.value = Constants.PAGE_IDLE
            }

            override fun onError(response: String) {
                _isUserPostsFetched.value = response
                _userPostsList.value = mutableListOf()
                _isLoadingMore.value = Constants.PAGE_IDLE
            }
        })
    }

    fun refreshUserPosts() {
        PostRepository.cancelGetRequest()
        PostRepository.userPostsList.value = null
        _isUserPostsFetched.value = null
        postsPage = 0
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