package com.example.discussions.adapters.interfaces

interface LikeCommentInterface {
    fun onLike(postOrPollId: String, isLiked: Boolean, btnLikeStatus: Boolean)
    fun onComment(id: String, type: String)
}