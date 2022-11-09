package com.example.discussions.models

data class PostModel(
    val id: String,
    val title: String,
    val content: String,
    val createdBy: String,
    val createdAt: String,
    val postImage: String,
    val likes: Int,
    val comments: Int
)