package com.example.discussions.repositories

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.discussions.api.ResponseCallback
import com.example.discussions.api.apiCalls.comments.*
import com.example.discussions.models.CommentModel
import com.example.discussions.store.LoginStore

class CommentsRepository {
    companion object {
        private const val TAG = "CommentsRepository"
        val commentsList = MutableLiveData<MutableList<CommentModel>?>(null)
        val hasMoreComments = MutableLiveData<Boolean>(false)

        fun getAllComments(
            context: Context,
            page: Int,
            postId: String?,
            pollId: String?,
            callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            GetCommentsApi.getCommentsJson(
                context,
                token,
                page,
                postId,
                pollId,
                object : ResponseCallback {
                    override fun onSuccess(response: String) {
                        val newCommentsList = GetCommentsApi.parseCommentsJson(response)
                        val oldCommentsList = commentsList.value ?: mutableListOf()
                        val updatedCommentsList = oldCommentsList.toMutableList()
                        updatedCommentsList.addAll(newCommentsList)

                        commentsList.value = updatedCommentsList
                        hasMoreComments.value = newCommentsList.isNotEmpty()
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

        fun cancelGetRequest() {
            GetCommentsApi.cancelGetRequest()
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

                        val updatedCommentsList =
                            addComment(cloneCommentList(commentsList.value), comment)

                        commentsList.postValue(updatedCommentsList)
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
            context: Context, commentId: String, content: String, callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            UpdateCommentApi.updateComment(
                context,
                token,
                commentId,
                content,
                object : ResponseCallback {
                    override fun onSuccess(response: String) {

                        val updatedCommentsList =
                            editComment(cloneCommentList(commentsList.value), commentId, content)

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

        fun deleteComment(
            context: Context, comment: CommentModel, callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            val oldCommentList = commentsList.value?.toMutableList()
            val updatedCommentsList = deleteComment(cloneCommentList(commentsList.value), comment)
            commentsList.value = updatedCommentsList

            DeleteCommentApi.deleteComment(
                context,
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

        fun likeComment(
            context: Context,
            commentId: String,
            callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            val oldCommentList = commentsList.value?.toMutableList()
            val updatedCommentsList = likeComment(cloneCommentList(commentsList.value), commentId)
            commentsList.value = updatedCommentsList

            CommentLikeApi.likeComment(
                context,
                commentId,
                token,
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
                }
            )
        }


        /**
         * METHOD FOR COMMENT's LIST DATA MANIPULATION
         */

        private fun cloneCommentList(comments: MutableList<CommentModel>?): MutableList<CommentModel>? {
            if (comments == null) return null
            val newComments = mutableListOf<CommentModel>()
            for (c in comments) {
                val newComment = CommentModel(
                    c.commentId,
                    c.count,
                    c.next,
                    c.previous,
                    c.parentCommentId,
                    c.comment,
                    c.username,
                    c.userImage,
                    c.createdAt,
                    c.isLiked,
                    c.likes,
                    cloneCommentList(c.replies.toMutableList()) ?: mutableListOf()
                )
                newComments.add(newComment)
            }
            return newComments
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
                    c.replies.add(0, comment)
                    return comments
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
                c.replies = editComment(c.replies, commentId, content)
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
                    c.replies.remove(comment)
                    return comments
                }
                c.replies = deleteComment(c.replies, comment)
            }
            return comments
        }

        private fun likeComment(
            comments: MutableList<CommentModel>?, commentId: String
        ): MutableList<CommentModel> {
            if (comments == null) return mutableListOf()
            for (c in comments) {
                if (c.commentId == commentId) {
                    val comment = c.copy(
                        isLiked = !c.isLiked,
                        likes = if (c.isLiked) c.likes - 1 else c.likes + 1
                    )
                    comments[comments.indexOf(c)] = comment
                    return comments
                }
                c.replies = likeComment(c.replies, commentId)
            }
            return comments
        }
    }
}