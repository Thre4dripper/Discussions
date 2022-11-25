package com.example.discussions.models

//this model is used by both creating poll recycler view and voting poll recycler view
data class PollOptionModel(
    val id: String,
    var content: String,
    val hint: String,
    val votes: Int = 0,
    val votedBy: List<PollVotedByModel> = listOf(),
)
