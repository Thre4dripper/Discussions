package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Constants
import com.example.discussions.MyApplication
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.PollModel
import com.example.discussions.repositories.DiscussionRepository
import com.example.discussions.repositories.PollRepository
import com.example.discussions.repositories.ProfileRepository

class PollDetailsViewModel : ViewModel() {
    private val TAG = "PollDetailsViewModel"

    private var _poll = PollRepository.singlePoll
    val poll: LiveData<PollModel?>
        get() = _poll

    private var _isPollFetched = MutableLiveData<String?>(null)
    val isPollFetched: MutableLiveData<String?>
        get() = _isPollFetched

    private var _isPollVoted = MutableLiveData<String?>(null)
    val isPollVoted: MutableLiveData<String?>
        get() = _isPollVoted

    private var _isPollDeleted = MutableLiveData<String?>(null)
    val isPollDeleted: LiveData<String?>
        get() = _isPollDeleted

    private var _isUsernameFetched = MutableLiveData<String>(null)
    val isUsernameFetched: LiveData<String>
        get() = _isUsernameFetched

    fun getUsername(context: Context) {
        ProfileRepository.getUsernameAndImage(context, object : ResponseCallback {
            override fun onSuccess(response: String) {
                ProfileRepository.map[Constants.USERNAME]?.let { MyApplication.username = it }
                _isUsernameFetched.postValue(Constants.API_SUCCESS)
            }

            override fun onError(response: String) {
                _isUsernameFetched.postValue(response)
            }
        })
    }

    fun isPollInAlreadyFetched(pollId: String): Boolean {
        //check if poll is in poll list
        return DiscussionRepository.discussions.value?.any { it.poll?.pollId == pollId }
        //if not, check if poll is in user poll list
            ?: PollRepository.userPollsList.value?.any { it.poll?.pollId == pollId }
            //if not, then poll is not in any list so far
            ?: false
    }

    fun getPollFromRepository(pollId: String) {
        _poll.value =
            DiscussionRepository.discussions.value?.find { it.poll?.pollId == pollId }?.poll
                ?: PollRepository.userPollsList.value?.find { it.poll!!.pollId == pollId }!!.poll
    }

    fun pollVote(context: Context, pollId: String, optionId: String) {
        //change the poll voting state
        val votedPoll = _poll.value!!.copy(isVoting = true)
        _poll.value = votedPoll

        //trigger the observer to update the UI
        _isPollVoted.value = null

        //send the vote to the server
        PollRepository.pollVote(context, pollId, optionId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPollVoted.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isPollVoted.value = response
            }
        })
    }

    fun likePoll(context: Context, postId: String) {
        PollRepository.likePoll(context, postId, object : ResponseCallback {
            override fun onSuccess(response: String) {}
            override fun onError(response: String) {}
        })
    }

    fun getPollFromApi(context: Context, pollId: String) {
        _isPollFetched.value = null
        PollRepository.getPollByID(context, pollId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPollFetched.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isPollFetched.value = response
            }
        })
    }

    fun deletePoll(context: Context, pollId: String) {
        _isPollDeleted.value = null
        PollRepository.deletePoll(context, pollId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPollDeleted.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isPollDeleted.value = Constants.API_FAILED
            }
        })
    }
}