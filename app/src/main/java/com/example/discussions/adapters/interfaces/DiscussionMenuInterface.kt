package com.example.discussions.adapters.interfaces

interface DiscussionMenuInterface {
    fun onPostEdit(postId: String) {}
    fun onPostDelete(postId: String) {}
    fun onPollDelete(pollId: String) {}
}