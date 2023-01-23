package com.example.discussions.repositories

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.discussions.api.ResponseCallback
import com.example.discussions.api.apiCalls.comments.CreateCommentApi
import com.example.discussions.api.apiCalls.comments.DeleteCommentApi
import com.example.discussions.api.apiCalls.comments.GetCommentsApi
import com.example.discussions.api.apiCalls.comments.UpdateCommentApi
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

            GetCommentsApi.getCommentsJson(context,
                token,
                postId,
                pollId,
                object : ResponseCallback {
                    override fun onSuccess(response: String) {
                        val comments = GetCommentsApi.parseCommentsJson(response)
                        //TODO remove after testing
                        //reverse the list to show the latest comments first temporarily
                        commentsList.postValue(comments.toMutableList())
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

            CreateCommentApi.createComment(context,
                token,
                postId,
                pollId,
                commentId,
                content,
                object : ResponseCallback {
                    override fun onSuccess(response: String) {
                        val comment = CreateCommentApi.parseCreateCommentJson(response)

                        val updatedCommentsList =
                            addComment(commentsList.value?.toMutableList(), comment)
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

        fun editComment(
            context: Context,
            commentId: String,
            content: String,
            callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            val oldCommentList = commentsList.value?.toMutableList()

            val updatedCommentsList =
                editComment(commentsList.value?.toMutableList(), commentId, content)
            commentsList.value = updatedCommentsList

            UpdateCommentApi.updateComment(context,
                token,
                commentId,
                content,
                object : ResponseCallback {
                    override fun onSuccess(response: String) {

                        callback.onSuccess(response)
                    }

                    override fun onError(response: String) {
                        //restore the old list on api error
                        commentsList.value = oldCommentList

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

        fun deleteComment(
            context: Context,
            comment: CommentModel,
            callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            val oldCommentList = commentsList.value?.toMutableList()
            val updatedCommentsList =
                deleteComment(commentsList.value?.toMutableList(), comment)
            commentsList.value = updatedCommentsList

            DeleteCommentApi.deleteComment(context,
                token,
                comment.commentId,
                object : ResponseCallback {
                    override fun onSuccess(response: String) {
                        callback.onSuccess(response)
                    }

                    override fun onError(response: String) {
                        //restore the old list on api error
                        commentsList.value = oldCommentList

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

        private fun addComment(
            comments: MutableList<CommentModel>?, comment: CommentModel
        ): MutableList<CommentModel> {
            if (comments == null) return mutableListOf(comment)
            if (comment.parentCommentId == null) {
                comments.add(0, comment)
                return comments
            }
            for (c in comments) {
                if (c.commentId == comment.parentCommentId) {
                    val replies = c.replies.toMutableList()
                    replies.add(comment)
                    c.replies = replies
                    return comments.toMutableList()
                }
                c.replies = addComment(c.replies.toMutableList(), comment)
            }
            return comments
        }

        private fun editComment(
            comments: MutableList<CommentModel>?, commentId: String, content: String
        ): MutableList<CommentModel> {
            if (comments == null) return mutableListOf()
            for (c in comments) {
                if (c.commentId == commentId) {
                    val comment = c.copy(comment = content)
                    comments[comments.indexOf(c)] = comment
                    return comments
                }
                c.replies = editComment(c.replies.toMutableList(), commentId, content)
            }
            return comments
        }

        private fun deleteComment(
            comments: MutableList<CommentModel>?, comment: CommentModel
        ): MutableList<CommentModel> {
            if (comments == null) return mutableListOf()
            if (comment.parentCommentId == null) {
                comments.remove(comment)
                return comments
            }
            for (c in comments) {
                if (c.commentId == comment.parentCommentId) {
                    val replies = c.replies.toMutableList()
                    replies.remove(comment)
                    c.replies = replies
                    return comments.toMutableList()
                }
                c.replies = deleteComment(c.replies.toMutableList(), comment)
            }
            return comments
        }
    }
}