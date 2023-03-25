package com.example.discussions.adapters.interfaces

interface LikeCommentInterface {
    fun onLike(
        postId: String?,
        pollId: String?,
        type: Int,
        isLiked: Boolean,
        btnLikeStatus: Boolean
    )

    fun onComment(postId: String?, pollId: String?, type: Int)
}