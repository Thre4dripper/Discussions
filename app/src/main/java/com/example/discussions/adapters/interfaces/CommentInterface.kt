package com.example.discussions.adapters.interfaces

import com.example.discussions.models.CommentModel

interface CommentInterface {
    fun onCommentLikeChanged(commentId: String)
    fun onCommentDeleted(comment: CommentModel)
    fun onCommentReply(commentId: String, username: String)
    fun onCommentEdit(commentId: String, content: String)
    fun onCommentCopy(content: String)
    fun onCommentLongClick(comment: CommentModel)
}