package com.example.discussions.models

data class PostModel(
    val postId: String,
    val title: String,
    val content: String,
    val username: String,
    val userImage: String,
    val createdAt: String,
    val postImage: String,
    val likes: Int,
    val comments: Int
)