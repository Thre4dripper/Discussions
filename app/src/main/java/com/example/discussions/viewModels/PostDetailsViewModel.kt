package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.PostModel
import com.example.discussions.repositories.CommentsRepository
import com.example.discussions.repositories.PostRepository

class PostDetailsViewModel : ViewModel() {
    private var _post: PostModel? = null
    val post: PostModel
        get() = _post!!

    var postComments = CommentsRepository.commentsList

    fun isPostInAlreadyFetched(postId: String): Boolean {
        //check if post is in post list
        return PostRepository.allPostsList.value?.any { it.postId == postId }
        //if not, check if post is in user post list
            ?: PostRepository.userPostsList.value?.any { it.postId == postId }
            //if not, then post is not in any list so far
            ?: false
    }

    fun getPostFromPostRepository(postId: String) {
        _post = PostRepository.allPostsList.value?.find { it.postId == postId }
            ?: PostRepository.userPostsList.value?.find { it.postId == postId }!!
    }

    fun likePost(context: Context, postId: String) {
        PostRepository.likePost(context, postId, object : ResponseCallback {
            override fun onSuccess(response: String) {}
            override fun onError(response: String) {}
        })
    }
}