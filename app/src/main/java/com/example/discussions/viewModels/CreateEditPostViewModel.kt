package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.repositories.DiscussionRepository
import com.example.discussions.repositories.PostRepository
import com.example.discussions.repositories.ProfileRepository

class CreateEditPostViewModel : ViewModel() {
    private val TAG = "CreateEditPostViewModel"

    var profileImage: String? = null
    var username: String = ""
    var postTitle: String = ""
    var postContent: String = ""
    var postImage: String = ""
    var allowComments: Boolean = true


    private var _isApiFetched = MutableLiveData<String>(null)
    val isApiFetched: LiveData<String>
        get() = _isApiFetched

    private var _isPostCreatedOrUpdated = MutableLiveData<String?>(null)
    val isPostCreatedOrUpdated: LiveData<String?>
        get() = _isPostCreatedOrUpdated

    private var _discussionsPosts = DiscussionRepository.discussions
    private var _userPosts = PostRepository.userPostsList


    fun getUsernameAndImage(context: Context) {
        ProfileRepository.getUsernameAndImage(context, object : ResponseCallback {
            override fun onSuccess(response: String) {
                ProfileRepository.map[Constants.PROFILE_IMAGE]?.let { profileImage = it }
                ProfileRepository.map[Constants.USERNAME]?.let { username = it }
                _isApiFetched.postValue(Constants.API_SUCCESS)
            }

            override fun onError(response: String) {
                _isApiFetched.postValue(response)
            }
        })
    }

    fun createPost(context: Context) {
        _isPostCreatedOrUpdated.postValue(null)
        PostRepository.createPost(
            context,
            postTitle,
            postContent,
            postImage,
            allowComments,
            object : ResponseCallback {
                override fun onSuccess(response: String) {
                    _isPostCreatedOrUpdated.postValue(Constants.API_SUCCESS)
                }

                override fun onError(response: String) {
                    _isPostCreatedOrUpdated.postValue(response)
                }
            })
    }

    fun editPost(context: Context, postId: String) {
        _isPostCreatedOrUpdated.postValue(null)
        PostRepository.updatePost(
            context,
            postId,
            postTitle,
            postContent,
            postImage,
            allowComments,
            object : ResponseCallback {
                override fun onSuccess(response: String) {
                    _isPostCreatedOrUpdated.postValue(Constants.API_SUCCESS)

                    //getting old post from all posts list, user posts list and single post
                    val oldPost = _discussionsPosts.value?.find { it.post?.postId == postId }
                    val oldUserPost = _userPosts.value?.find { it.post?.postId == postId }
                    val singlePost = PostRepository.singlePost.value?.let {
                        if (it.postId == postId) it else null
                    }

                    //when post is in all posts list
                    if (oldPost != null) {
                        //creating copy of the post from all posts list
                        val newPost = oldPost.post!!.copy(
                            title = postTitle,
                            content = postContent,
                            postImage = postImage,
                        )
                        val newDiscussionPost = oldPost.copy(
                            post = newPost
                        )

                        val newAllPosts = _discussionsPosts.value?.toMutableList()
                        newAllPosts?.set(newAllPosts.indexOf(oldPost), newDiscussionPost)

                        _discussionsPosts.postValue(newAllPosts)
                    }

                    //when post is in user posts list
                    if (oldUserPost != null) {
                        //creating copy of the post from user posts list
                        val newUserPost = oldUserPost.post!!.copy(
                            title = postTitle,
                            content = postContent,
                            postImage = postImage,
                        )

                        val newUserDiscussionPost = oldUserPost.copy(
                            post = newUserPost
                        )

                        val newUserPosts = _userPosts.value?.toMutableList()
                        newUserPosts?.set(newUserPosts.indexOf(oldUserPost), newUserDiscussionPost)

                        _userPosts.postValue(newUserPosts)
                    }

                    //when post is in single post
                    if (singlePost != null) {
                        //creating copy of the post from single post
                        val newSinglePost = singlePost.copy(
                            title = postTitle,
                            content = postContent,
                            postImage = postImage,
                        )

                        PostRepository.singlePost.postValue(newSinglePost)
                    }
                }

                override fun onError(response: String) {
                    _isPostCreatedOrUpdated.postValue(response)
                }
            })
    }
}