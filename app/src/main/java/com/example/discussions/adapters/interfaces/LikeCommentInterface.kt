package com.example.discussions.adapters.interfaces

interface LikeCommentInterface {
    fun onPostLike(postId: String, isLiked: Boolean, btnLikeStatus: Boolean) {}
    fun onPostComment(postId: String) {}
    fun onPollLike(pollId: String, isLiked: Boolean, btnLikeStatus: Boolean) {}
    fun onPollComment(pollId: String) {}
}