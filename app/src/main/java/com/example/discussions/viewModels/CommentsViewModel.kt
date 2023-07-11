package com.example.discussions.viewModels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.CommentModel
import com.example.discussions.repositories.CommentsRepository
import com.example.discussions.repositories.DiscussionRepository
import com.example.discussions.repositories.PollRepository
import com.example.discussions.repositories.PostRepository

class CommentsViewModel : ViewModel() {
    private val TAG = "CommentsViewModel"

    var commentsList = CommentsRepository.commentsList

    private var _isCommentsFetched = MutableLiveData<String?>(null)
    val isCommentsFetched: LiveData<String?>
        get() = _isCommentsFetched

    private var _isCommentAdded = MutableLiveData<String?>(null)
    val isCommentAdded: LiveData<String?>
        get() = _isCommentAdded

    private var _isCommentEdited = MutableLiveData<String?>(null)
    val isCommentEdited: LiveData<String?>
        get() = _isCommentEdited

    private var _isCommentDeleted = MutableLiveData<String?>(null)
    val isCommentDeleted: LiveData<String?>
        get() = _isCommentDeleted

    private var _isCommentLikedChanged = MutableLiveData<String?>(null)
    val isCommentLikedChanged: LiveData<String?>
        get() = _isCommentLikedChanged

    /**
     * PAGINATION STUFF
     */
    private var commentsPage = 0
    val hasMoreComments = CommentsRepository.hasMoreComments

    private var _paginationStatus = MutableLiveData(Constants.PAGE_IDLE)
    val paginationStatus: LiveData<String?>
        get() = _paginationStatus


    companion object {
        var commentsScrollToTop = false
    }

    fun getComments(context: Context, postId: String?, pollId: String?) {
        _isCommentsFetched.value = null
        _paginationStatus.value = Constants.PAGE_LOADING

        commentsPage++
        CommentsRepository.getAllComments(
            context,
            commentsPage,
            postId,
            pollId,
            object : ResponseCallback {
                override fun onSuccess(response: String) {
                    _isCommentsFetched.value = Constants.API_SUCCESS
                    _paginationStatus.value = Constants.PAGE_IDLE
                }

                override fun onError(response: String) {
                    _isCommentsFetched.value = response
                    val alternateList = commentsList.value?.toMutableList() ?: mutableListOf()
                    commentsList.value = alternateList
                    _paginationStatus.value = Constants.PAGE_IDLE

                    Toast.makeText(context, response, Toast.LENGTH_SHORT).show()
                }
            })
    }

    fun refreshAllComments() {
        CommentsRepository.cancelGetRequest()
        CommentsRepository.commentsList.value = null
        commentsPage = 0
        _paginationStatus.value = Constants.PAGE_IDLE
    }

    fun createComment(
        context: Context,
        postId: String?,
        pollId: String?,
        commentId: String?,
        content: String
    ) {
        _isCommentAdded.value = null

        //scroll to top only when new comment is added
        commentsScrollToTop = commentId == null
        CommentsRepository.createComment(
            context,
            postId,
            pollId,
            commentId,
            content,
            object : ResponseCallback {
                override fun onSuccess(response: String) {
                    _isCommentAdded.value = Constants.API_SUCCESS
                }

                override fun onError(response: String) {
                    _isCommentAdded.value = response
                }
            })
    }

    fun editComment(context: Context, commentId: String, content: String) {
        _isCommentEdited.value = null
        commentsScrollToTop = false
        CommentsRepository.editComment(context, commentId, content, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isCommentEdited.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isCommentEdited.value = response
            }
        })
    }

    fun deleteComment(context: Context, comment: CommentModel) {
        _isCommentDeleted.value = null
        commentsScrollToTop = false
        CommentsRepository.deleteComment(context, comment, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isCommentDeleted.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isCommentDeleted.value = response
            }
        })
    }

    fun likeComment(context: Context, commentId: String) {
        _isCommentLikedChanged.value = null
        commentsScrollToTop = false
        CommentsRepository.likeComment(context, commentId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isCommentLikedChanged.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isCommentLikedChanged.value = response
            }
        })
    }

    fun getPostLikeStatus(postId: String): Boolean {
        DiscussionRepository.discussions.value?.forEach { it ->
            if (it.post?.postId == postId) {
                return it.post.isLiked
            }
        }
        PostRepository.userPostsList.value?.forEach {
            if (it.post?.postId == postId) {
                return it.post.isLiked
            }
        }
        return false
    }

    fun getPollLikeStatus(pollId: String): Boolean {
        DiscussionRepository.discussions.value?.forEach { it ->
            if (it.poll?.pollId == pollId) {
                return it.poll.isLiked
            }
        }
        PollRepository.userPollsList.value?.forEach {
            if (it.poll?.pollId == pollId) {
                return it.poll.isLiked
            }
        }
        return false
    }

    fun likePost(context: Context, postId: String) {
        commentsScrollToTop = false
        PostRepository.likePost(context, postId, object : ResponseCallback {
            override fun onSuccess(response: String) {}
            override fun onError(response: String) {}
        })
    }

    fun likePoll(context: Context, pollId: String) {
        commentsScrollToTop = false
        PollRepository.likePoll(context, pollId, object : ResponseCallback {
            override fun onSuccess(response: String) {}
            override fun onError(response: String) {}
        })
    }
}