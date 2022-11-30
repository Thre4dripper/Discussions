package com.example.discussions.adapters.interfaces

interface LikeCommentInterface {
    fun onLike(postOrPollId: String)
    fun onComment(postOrPollId: String)
}