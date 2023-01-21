package com.example.discussions.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.repositories.CommentsRepository

class CommentsViewModel : ViewModel() {
    private val TAG = "CommentsViewModel"

    var commentsList = CommentsRepository.commentsList

    private var _isCommentsFetched = MutableLiveData<String?>(null)
    val isCommentsFetched: LiveData<String?>
        get() = _isCommentsFetched

    private var _isCommentAdded = MutableLiveData<String?>(null)
    val isCommentAdded: LiveData<String?>
        get() = _isCommentAdded

    private var _isCommentLikedChanged = MutableLiveData<String?>(null)
    val isCommentLikedChanged: LiveData<String?>
        get() = _isCommentLikedChanged

    private var _isCommentDeleted = MutableLiveData<String?>(null)
    val isCommentDeleted: LiveData<String?>
        get() = _isCommentDeleted

    fun getComments(context: Context, postId: Int?, pollId: Int?) {
        _isCommentsFetched.value = null
        CommentsRepository.getAllComments(context, postId, pollId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                Log.d(TAG, "onSuccess: $response")
                _isCommentsFetched.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isCommentsFetched.value = response
                commentsList.value = null
            }
        })
    }

    fun refreshComments(context: Context, postId: Int?, pollId: Int?) {
        _isCommentsFetched.value = null
        getComments(context, postId, pollId)
    }
}