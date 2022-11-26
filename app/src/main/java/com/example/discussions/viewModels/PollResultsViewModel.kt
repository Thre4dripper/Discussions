package com.example.discussions.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.models.PollModel
import com.example.discussions.models.PollOptionModel
import com.example.discussions.models.PollVotedByModel
import com.example.discussions.repositories.PollRepository

class PollResultsViewModel : ViewModel() {
    lateinit var poll: PollModel
    lateinit var pollQuestion: String
    lateinit var pollOptions: List<PollOptionModel>

    private var _votedByList = MutableLiveData<MutableList<PollVotedByModel>>()
    val votedByList: LiveData<MutableList<PollVotedByModel>>
        get() = _votedByList

    fun getPollDetails(pollId: String) {
        poll = PollRepository.userPollsList.value!!.find { it.pollId == pollId }!!.copy()
        pollQuestion = poll.content
        pollOptions = poll.pollOptions
    }

    fun getVotedByList(optionId: String) {
        _votedByList.value = pollOptions.find { it.id == optionId }!!.votedBy.toMutableList()
    }
}