package com.example.discussions.adapters.interfaces

interface LikeCommentInterface {
    fun onLike(postOrPollId: String)
    fun onComment(id: String, type: String)
}