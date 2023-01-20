package com.example.discussions.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.models.CommentsModel

class CommentsViewModel : ViewModel() {
    private var _commentsList = MutableLiveData<MutableList<CommentsModel>>()
    val commentsList: MutableLiveData<MutableList<CommentsModel>>
        get() = _commentsList

    private var _isCommentsFetched = MutableLiveData<String>(null)
    val isCommentsFetched: MutableLiveData<String>
        get() = _isCommentsFetched

    private var _isCommentAdded = MutableLiveData<String>(null)
    val isCommentAdded: MutableLiveData<String>
        get() = _isCommentAdded

    private var _isCommentLikedChanged = MutableLiveData<String>(null)
    val isCommentLikedChanged: MutableLiveData<String>
        get() = _isCommentLikedChanged

    private var _isCommentDeleted = MutableLiveData<String>(null)
    val isCommentDeleted: MutableLiveData<String>
        get() = _isCommentDeleted
}