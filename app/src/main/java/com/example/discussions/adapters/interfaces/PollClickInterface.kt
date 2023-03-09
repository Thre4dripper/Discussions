package com.example.discussions.adapters.interfaces

interface PollClickInterface {
    fun onPollVote(pollId: String, optionId: String)
    fun onPollResult(pollId: String)
    fun onPollClick(pollId: String)
}