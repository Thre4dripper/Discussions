package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.PostModel
import com.example.discussions.repositories.DiscussionRepository
import com.example.discussions.repositories.PostRepository

class PostDetailsViewModel : ViewModel() {
    private var _post = PostRepository.singlePost
    val post: LiveData<PostModel?>
        get() = _post

    private var _isPostFetched = MutableLiveData<String?>(null)
    val isPostFetched: MutableLiveData<String?>
        get() = _isPostFetched

    fun isPostInAlreadyFetched(postId: String): Boolean {
        //check if post is in post list
        return DiscussionRepository.discussions.value?.any { it.post?.postId == postId }
        //if not, check if post is in user post list
            ?: PostRepository.userPostsList.value?.any { it.post?.postId == postId }
            //if not, then post is not in any list so far
            ?: false
    }

    fun getPostFromRepository(postId: String) {
        _post.value = DiscussionRepository.discussions.value?.find { it.post?.postId == postId }?.post
            ?: PostRepository.userPostsList.value!!.find { it.post!!.postId == postId }!!.post
    }

    fun likePost(context: Context, postId: String) {
        PostRepository.likePost(context, postId, object : ResponseCallback {
            override fun onSuccess(response: String) {}
            override fun onError(response: String) {}
        })
    }

    fun getPostFromApi(context: Context, postId: String) {
        _isPostFetched.value = null
        PostRepository.getPostByID(context, postId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPostFetched.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isPostFetched.value = response
            }
        })
    }
}