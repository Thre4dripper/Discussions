package com.example.discussions.adapters.interfaces

import com.example.discussions.models.CommentModel

interface CommentInterface {
    fun onCommentLikeChanged(commentId: String, isLiked: Boolean)
    fun onCommentDeleted(commentId: String)
    fun onCommentReply(commentId: String, username: String)
    fun onCommentEdit(commentId: String)
    fun onCommentCopy(content: String)
    fun onCommentLongClick(comment: CommentModel)
}