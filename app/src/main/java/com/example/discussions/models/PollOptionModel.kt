package com.example.discussions.models

//this model is used by both creating poll recycler view and voting poll recycler view
data class PollOptionModel(
    val id: String,
    var content: String,
    val hint: String,
    val votes: Int,
    val votedBy: List<PollVotedByModel>,
)
{
    //this constructor is used by creating poll recycler view
    constructor(
        id: String,
        content: String,
        hint: String,
    ) : this(id, content, hint, 0, listOf())

    //default constructor is used by voting poll recycler view
}
