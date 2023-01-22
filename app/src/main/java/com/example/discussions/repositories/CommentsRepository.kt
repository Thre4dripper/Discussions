package com.example.discussions.repositories

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.discussions.api.ResponseCallback
import com.example.discussions.api.apiCalls.comments.CreateCommentApi
import com.example.discussions.api.apiCalls.comments.GetCommentsApi
import com.example.discussions.models.CommentModel
import com.example.discussions.store.LoginStore

class CommentsRepository {
    companion object {
        private const val TAG = "CommentsRepository"
        val commentsList = MutableLiveData<MutableList<CommentModel>?>(null)

        fun getAllComments(
            context: Context, postId: String?, pollId: String?, callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            GetCommentsApi.getCommentsJson(
                context,
                token,
                postId,
                pollId,
                object : ResponseCallback {
                    override fun onSuccess(response: String) {
                        val comments = GetCommentsApi.parseCommentsJson(response)
                        //TODO remove after testing
                        //reverse the list to show the latest comments first temporarily
                        commentsList.postValue(comments.reversed().toMutableList())
                        callback.onSuccess(response)
                    }

                    override fun onError(response: String) {
                        if (response.contains("com.android.volley.TimeoutError")) {
                            callback.onError("Time Out")
                        } else if (response.contains("com.android.volley.NoConnectionError")) {
                            callback.onError("Please check your internet connection")
                        } else if (response.contains("com.android.volley.AuthFailureError")) {
                            callback.onError("Auth Error")
                        } else if (response.contains("com.android.volley.NetworkError")) {
                            callback.onError("Network Error")
                        } else if (response.contains("com.android.volley.ServerError")) {
                            callback.onError("Server Error")
                        } else if (response.contains("com.android.volley.ParseError")) {
                            callback.onError("Parse Error")
                        } else {
                            callback.onError("Something went wrong")
                        }
                    }
                })

        }

        fun createComment(
            context: Context,
            postId: String?,
            pollId: String?,
            commentId: String?,
            content: String,
            callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            CreateCommentApi.createComment(
                context,
                token,
                postId,
                pollId,
                commentId,
                content,
                object : ResponseCallback {
                    override fun onSuccess(response: String) {
                        val comment = CreateCommentApi.parseCreateCommentJson(response)

                        fun addComment(
                            comments: MutableList<CommentModel>?, comment: CommentModel
                        ): MutableList<CommentModel>? {
                            if (comment.parentCommentId == null) {
                                comments?.add(0, comment)
                                return comments
                            }
                            for (c in comments!!) {
                                if (c.commentId == comment.parentCommentId) {
                                    c.replies.add(0, comment)
                                    return comments
                                } else addComment(c.replies, comment)
                            }
                            return comments
                        }

                        val updatedCommentsList = addComment(commentsList.value, comment)
                        commentsList.value = null
                        commentsList.value = updatedCommentsList
                        callback.onSuccess(response)
                    }

                    override fun onError(response: String) {
                        if (response.contains("com.android.volley.TimeoutError")) {
                            callback.onError("Time Out")
                        } else if (response.contains("com.android.volley.NoConnectionError")) {
                            callback.onError("Please check your internet connection")
                        } else if (response.contains("com.android.volley.AuthFailureError")) {
                            callback.onError("Auth Error")
                        } else if (response.contains("com.android.volley.NetworkError")) {
                            callback.onError("Network Error")
                        } else if (response.contains("com.android.volley.ServerError")) {
                            callback.onError("Server Error")
                        } else if (response.contains("com.android.volley.ParseError")) {
                            callback.onError("Parse Error")
                        } else {
                            callback.onError("Something went wrong")
                        }
                    }
                })

        }
    }
}