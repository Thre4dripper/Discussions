package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.DiscussionModel
import com.example.discussions.repositories.DiscussionRepository
import com.example.discussions.repositories.PollRepository

class PollsViewModel : ViewModel() {
    private val TAG = "PollsViewModel"

    //all polls list from repository
    private var _allPollsList = DiscussionRepository.discussions

    //get user list directly from repository live data
    private var _userPollsList = PollRepository.userPollsList
    val userPollsList: LiveData<MutableList<DiscussionModel>?>
        get() = _userPollsList

    private var _isUserPollsFetched = MutableLiveData<String?>(null)
    val isUserPollsFetched: LiveData<String?>
        get() = _isUserPollsFetched

    private var _isPollDeleted = MutableLiveData<String?>(null)
    val isPollDeleted: LiveData<String?>
        get() = _isPollDeleted

    private var _isPollLikedChanged = MutableLiveData<String?>(null)
    val isPollLikedChanged: LiveData<String?>
        get() = _isPollLikedChanged

    private var _isPollVoted = MutableLiveData<String?>(null)
    val isPollVoted: LiveData<String?>
        get() = _isPollVoted

    /**
     * PAGINATION STUFF
     */
    private var pollsPage = 0
    val hasMorePolls = PollRepository.hasMorePolls

    private var _isLoadingMore = MutableLiveData(Constants.PAGE_IDLE)
    val isLoadingMore: LiveData<String?>
        get() = _isLoadingMore

    companion object {
        //TODO use this in user polls activity
        var userPollsScrollToIndex = false
    }


    fun getAllUserPolls(context: Context) {
        _isUserPollsFetched.value = null
        _isLoadingMore.value = Constants.PAGE_LOADING

        pollsPage++
        PollRepository.getAllUserPolls(context, pollsPage, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isUserPollsFetched.value = Constants.API_SUCCESS
                _isLoadingMore.value = Constants.PAGE_IDLE
            }

            override fun onError(response: String) {
                _isUserPollsFetched.value = response
                _userPollsList.value = mutableListOf()
                _isLoadingMore.value = Constants.PAGE_IDLE
            }
        })
    }

    fun refreshAllUserPolls(context: Context) {
        PollRepository.cancelGetRequest()
        PollRepository.userPollsList.value = null
        pollsPage = 0
        getAllUserPolls(context)
    }

    fun deletePoll(context: Context, pollId: String) {
        _isPollDeleted.value = null
        userPollsScrollToIndex = false
        HomeViewModel.postsOrPollsOrNotificationsScrollToTop = false

        PollRepository.deletePoll(context, pollId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPollDeleted.postValue(Constants.API_SUCCESS)
            }

            override fun onError(response: String) {
                _isPollDeleted.postValue(Constants.API_FAILED)
            }
        })
    }

    fun likePoll(context: Context, pollId: String) {
        _isPollLikedChanged.value = null
        userPollsScrollToIndex = false
        HomeViewModel.postsOrPollsOrNotificationsScrollToTop = false

        PollRepository.likePoll(context, pollId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPollLikedChanged.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isPollLikedChanged.value = Constants.API_FAILED
            }
        })
    }


    fun pollVote(context: Context, pollId: String, optionId: String) {
        _isPollVoted.value = null
        userPollsScrollToIndex = false
        HomeViewModel.postsOrPollsOrNotificationsScrollToTop = false

        //changing vote status to voting, this will trigger progress bar in recycler view
        val votedPoll = _allPollsList.value?.find { it.poll!!.pollId == pollId }
        val votedPollIndex: Int
        val newPollsList: MutableList<DiscussionModel>

        if (votedPoll != null) {
            votedPollIndex = _allPollsList.value!!.indexOf(votedPoll)
            newPollsList = _allPollsList.value!!.toMutableList()
            val newVotedPoll = votedPoll.poll!!.copy(isVoting = true)
            val newDiscussionPoll = votedPoll.copy(poll = newVotedPoll)
            newPollsList[votedPollIndex] = newDiscussionPoll
            _allPollsList.value = newPollsList
        }

        val votedUserPoll = _userPollsList.value?.find { it.poll!!.pollId == pollId }
        val votedUserPollIndex: Int
        val newUserPollsList: MutableList<DiscussionModel>

        if (votedUserPoll != null) {
            votedUserPollIndex = _userPollsList.value!!.indexOf(votedUserPoll)
            newUserPollsList = _userPollsList.value!!.toMutableList()
            val newVotedUserPoll = votedUserPoll.poll!!.copy(isVoting = true)
            val newDiscussionUserPoll = votedUserPoll.copy(poll = newVotedUserPoll)
            newUserPollsList[votedUserPollIndex] = newDiscussionUserPoll
            _userPollsList.value = newUserPollsList
        }

        PollRepository.pollVote(context, pollId, optionId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPollVoted.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isPollVoted.value = Constants.API_FAILED
            }
        })
    }
}