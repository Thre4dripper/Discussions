package com.example.discussions.models

data class PollModel(
    val pollId: String,
    val title: String,
    val content: String,
    val totalVotes: Int,
    val isPrivate: Boolean,
    val isVoted: Boolean,
    val username: String,
    val userImage: String,
    val createdAt: String,
    val isLiked: Boolean,
    val likes: Int,
    val allowComments: Boolean,
    val comments: Int,
    val pollOptions: List<PollOptionModel>,
    val isVoting: Boolean = false,
)