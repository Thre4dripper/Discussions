package com.example.discussions.models

data class DiscussionModel(
    val id: String,
    val count: Int,
    val next: String?,
    val previous: String?,
    val post: PostModel?,
    val poll: PollModel?,
)