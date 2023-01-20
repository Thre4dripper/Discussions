package com.example.discussions.models

data class ReplyCommentModel(
    val commentId: String,
    val replyId: String,
    val reply: String,
    val username: String,
    val userImage: String,
    val createdAt: String
)