package com.example.discussions.models

data class CommentModel(
    val commentId: String,
    val parentCommentId: String?,
    val comment: String,
    val username: String,
    val userImage: String,
    val createdAt: String,
    val replies: MutableList<CommentModel>
)