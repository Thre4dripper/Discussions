package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Cloudinary
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.PostModel
import com.example.discussions.repositories.PostRepository

class UserPostsViewModel : ViewModel() {

    private val TAG = "UserPostsViewModel"

    //get user list directly from repository live data
    private var _userPosts = PostRepository.userPostsList
    val userPosts: LiveData<MutableList<PostModel>?>
        get() = _userPosts

    //all posts list from repository
    private var _allPosts = PostRepository.allPostsList

    private var _isPostDeleted = MutableLiveData<String?>(null)
    val isPostDeleted: LiveData<String?>
        get() = _isPostDeleted

    fun deletePost(context: Context, postId: String) {
        _isPostDeleted.value = null

        //deleting post from all posts list
        val deletedPost = _allPosts.value!!.find { it.postId == postId }!!
        val deletedPostIndex = _allPosts.value!!.indexOf(deletedPost)
        var newPostsList = _allPosts.value!!.toMutableList()
        newPostsList.removeAt(deletedPostIndex)
        _allPosts.value = newPostsList

        //deleting post from user posts list
        val deletedUserPost = _userPosts.value!!.find { it.postId == postId }!!
        val deletedUserPostIndex = _userPosts.value!!.indexOf(deletedUserPost)
        var newUserPostsList = _userPosts.value!!.toMutableList()
        newUserPostsList.removeAt(deletedUserPostIndex)
        _userPosts.value = newUserPostsList


        PostRepository.deletePost(context, postId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPostDeleted.postValue(Constants.API_SUCCESS)

                val imageUrl = deletedPost.postImage
                Cloudinary.deleteImage(context, imageUrl)
            }

            override fun onError(response: String) {
                _isPostDeleted.postValue(Constants.API_FAILED)

                //re-adding post when error occurs
                newPostsList = _allPosts.value!!.toMutableList()
                newPostsList.add(deletedPostIndex, deletedPost)
                _allPosts.value = newPostsList

                newUserPostsList = _userPosts.value!!.toMutableList()
                newUserPostsList.add(deletedUserPostIndex, deletedUserPost)
                _userPosts.value = newUserPostsList
            }
        })
    }
}