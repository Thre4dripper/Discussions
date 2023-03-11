package com.example.discussions.adapters.interfaces

import com.example.discussions.models.PollModel
import com.example.discussions.models.PostModel

interface DiscussionMenuInterface {
    fun onPostEdit(postId: String) {}
    fun onPostDelete(postId: String) {}
    fun onPollDelete(pollId: String) {}
    fun onPostMenuClicked(post: PostModel) {}
    fun onPollMenuClicked(poll: PollModel) {}
}