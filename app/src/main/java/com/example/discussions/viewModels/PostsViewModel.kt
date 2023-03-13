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
    private var _allPostsList = DiscussionRepository.discussions

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

        //deleting post from all posts list
        val deletedPost = _allPostsList.value?.find { it.post?.postId == postId }
        var deletedPostIndex = -1
        var newPostsList: MutableList<DiscussionModel>

        //when all posts list is not updated yet after inserting new post then deleted post can only be found in user posts list
        if (deletedPost != null) {
            deletedPostIndex = _allPostsList.value!!.indexOf(deletedPost)
            newPostsList = _allPostsList.value!!.toMutableList()
            newPostsList.removeAt(deletedPostIndex)
            _allPostsList.value = newPostsList
        }

        //deleting post from user posts list
        val deletedUserPost = _userPostsList.value?.find { it.post?.postId == postId }
        var deletedUserPostIndex = -1
        var newUserPostsList: MutableList<DiscussionModel>

        if (deletedUserPost != null) {
            deletedUserPostIndex = _userPostsList.value!!.indexOf(deletedUserPost)
            newUserPostsList = _userPostsList.value!!.toMutableList()
            newUserPostsList.removeAt(deletedUserPostIndex)
            _userPostsList.value = newUserPostsList
        }

        PostRepository.deletePost(context, postId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPostDeleted.postValue(Constants.API_SUCCESS)

                val imageUrl =
                    deletedPost?.post?.postImage ?: deletedUserPost?.post?.postImage ?: ""
                if (imageUrl.isNotEmpty()) Cloudinary.deleteImage(context, imageUrl)
            }

            override fun onError(response: String) {
                _isPostDeleted.postValue(Constants.API_FAILED)

                if (deletedPost != null) {
                    //re-adding post when error occurs
                    newPostsList = _allPostsList.value!!.toMutableList()
                    newPostsList.add(deletedPostIndex, deletedPost)
                    _allPostsList.value = newPostsList
                }

                if (deletedUserPost != null) {
                    //re-adding post when error occurs
                    newUserPostsList = _userPostsList.value!!.toMutableList()
                    newUserPostsList.add(deletedUserPostIndex, deletedUserPost)
                    _userPostsList.value = newUserPostsList
                }
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