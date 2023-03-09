package com.example.discussions.adapters.interfaces

interface DiscussionMenuInterface {
    fun onEdit(postOrPollId: String)
    fun onDelete(postOrPollId: String)
}