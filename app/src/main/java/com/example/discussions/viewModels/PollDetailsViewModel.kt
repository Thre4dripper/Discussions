package com.example.discussions.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.models.PollModel
import com.example.discussions.repositories.PollRepository

class PollDetailsViewModel : ViewModel() {
    private var _poll = PollRepository.singlePoll
    val poll: LiveData<PollModel?>
        get() = _poll

    private var _isPollFetched = MutableLiveData<String?>(null)
    val isPollFetched: MutableLiveData<String?>
        get() = _isPollFetched

    fun isPollInAlreadyFetched(pollId: String): Boolean {
        //check if poll is in poll list
        return PollRepository.allPollsList.value?.any { it.pollId == pollId }
        //if not, check if poll is in user poll list
            ?: PollRepository.userPollsList.value?.any { it.pollId == pollId }
            //if not, then poll is not in any list so far
            ?: false
    }

    fun getPollFromPollRepository(pollId: String) {
        _poll.value = PollRepository.allPollsList.value?.find { it.pollId == pollId }
            ?: PollRepository.userPollsList.value?.find { it.pollId == pollId }!!
    }
}