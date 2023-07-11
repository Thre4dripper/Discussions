package com.example.discussions.models

data class CommentModel(
    val commentId: String,
    val count: Int,
    val next: String?,
    val previous: String?,
    val parentCommentId: String?,
    val comment: String,
    val username: String,
    val userImage: String,
    val createdAt: String,
    val isLiked: Boolean,
    val likes: Int,
    var replies: MutableList<CommentModel>
)