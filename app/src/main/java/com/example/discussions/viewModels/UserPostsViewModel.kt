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

class UserPostsViewModel : ViewModel() {

    private val TAG = "UserPostsViewModel"

    //get user list directly from repository live data
    private var _userPosts = PostRepository.userPostsList
    val userPosts: LiveData<MutableList<DiscussionModel>?>
        get() = _userPosts

    //all posts list from repository
    private var _allPosts = DiscussionRepository.discussions

    private var _isPostDeleted = MutableLiveData<String?>(null)
    val isPostDeleted: LiveData<String?>
        get() = _isPostDeleted

    private var _isPostLikedChanged = MutableLiveData<String?>(null)
    val isPostLikedChanged: LiveData<String?>
        get() = _isPostLikedChanged

    companion object {
        var userPostsScrollToIndex = false
    }

    fun deletePost(context: Context, postId: String) {
        _isPostDeleted.value = null

        //deleting post from all posts list
        val deletedPost = _allPosts.value!!.find { it.post?.postId == postId }
        var deletedPostIndex = -1
        var newPostsList: MutableList<DiscussionModel>

        //when all posts list is not updated yet after inserting new post then deleted post can only be found in user posts list
        if (deletedPost != null) {
            deletedPostIndex = _allPosts.value!!.indexOf(deletedPost)
            newPostsList = _allPosts.value!!.toMutableList()
            newPostsList.removeAt(deletedPostIndex)
            _allPosts.value = newPostsList
        }

        //deleting post from user posts list
        val deletedUserPost = _userPosts.value!!.find { it.post?.postId == postId }!!
        val deletedUserPostIndex = _userPosts.value!!.indexOf(deletedUserPost)
        var newUserPostsList = _userPosts.value!!.toMutableList()
        newUserPostsList.removeAt(deletedUserPostIndex)
        _userPosts.value = newUserPostsList


        PostRepository.deletePost(context, postId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPostDeleted.postValue(Constants.API_SUCCESS)

                val imageUrl = deletedUserPost.post!!.postImage
                if (imageUrl.isNotEmpty()) Cloudinary.deleteImage(context, imageUrl)
            }

            override fun onError(response: String) {
                _isPostDeleted.postValue(Constants.API_FAILED)

                if (deletedPost != null) {
                    //re-adding post when error occurs
                    newPostsList = _allPosts.value!!.toMutableList()
                    newPostsList.add(deletedPostIndex, deletedPost)
                    _allPosts.value = newPostsList
                }

                newUserPostsList = _userPosts.value!!.toMutableList()
                newUserPostsList.add(deletedUserPostIndex, deletedUserPost)
                _userPosts.value = newUserPostsList
            }
        })
    }

    fun likePost(context: Context, postId: String) {
        _isPostLikedChanged.value = null
        userPostsScrollToIndex = false

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