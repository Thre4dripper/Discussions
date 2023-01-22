package com.example.discussions.adapters.interfaces

interface CommentInterface {
    fun onCommentLikeChanged(commentId: String, isLiked: Boolean)
    fun onCommentDeleted(commentId: String)
    fun onCommentReply(commentId: String)
    fun onCommentEdit(commentId: String, content: String)
}