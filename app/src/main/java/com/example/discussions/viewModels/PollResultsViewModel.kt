package com.example.discussions.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.models.PollModel
import com.example.discussions.models.PollOptionModel
import com.example.discussions.models.PollVotedByModel
import com.example.discussions.repositories.DiscussionRepository
import com.example.discussions.repositories.PollRepository

class PollResultsViewModel : ViewModel() {
    lateinit var poll: PollModel
    lateinit var pollQuestion: String
    lateinit var pollOptions: List<PollOptionModel>

    private var _votedByList = MutableLiveData<MutableList<PollVotedByModel>>()
    val votedByList: LiveData<MutableList<PollVotedByModel>>
        get() = _votedByList

    fun getPollDetails(pollId: String) {
        //TODO handle all list ,  user list and single list case
        poll =
            DiscussionRepository.discussions.value?.find { it.poll?.pollId == pollId }?.poll?.copy()
                ?: PollRepository.userPollsList.value?.find { it.poll!!.pollId == pollId }!!.poll!!.copy()
        pollQuestion = poll.content
        pollOptions = poll.pollOptions
    }

    fun getVotedByList(optionIndex: Int) {
        _votedByList.value = pollOptions[optionIndex].votedBy.toMutableList()
    }
}