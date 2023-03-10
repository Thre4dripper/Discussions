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

class UserPollsViewModel : ViewModel() {
    private val TAG = "UserPollsViewModel"

    //TODO make it like user posts view model separate all polls list and user polls list
    //get user list directly from repository live data
    private var _userPolls = PollRepository.userPollsList

    private var _allPolls = DiscussionRepository.discussions

    private var _isPollDeleted = MutableLiveData<String?>(null)
    val isPollDeleted: LiveData<String?>
        get() = _isPollDeleted

    fun deletePoll(context: Context, pollId: String) {
        _isPollDeleted.value = null
        HomeViewModel.postsOrPollsOrNotificationsScrollToTop = false

        //deleting poll from all polls list
        val deletedPoll = _allPolls.value!!.find { it.poll!!.pollId == pollId }
        var deletedPollIndex = -1
        var newPollsList: MutableList<DiscussionModel>

        //when all polls list is not updated yet after inserting new poll then deleted poll can only be found in user polls list
        if (deletedPoll != null) {
            deletedPollIndex = _allPolls.value!!.indexOf(deletedPoll)
            newPollsList = _allPolls.value!!.toMutableList()
            newPollsList.removeAt(deletedPollIndex)
            _allPolls.value = newPollsList
        }

        //deleting poll from user polls list
        val deletedUserPoll = _userPolls.value!!.find { it.poll!!.pollId == pollId }!!
        val deletedUserPollIndex = _userPolls.value!!.indexOf(deletedUserPoll)
        var newUserPollsList = _userPolls.value!!.toMutableList()
        newUserPollsList.removeAt(deletedUserPollIndex)
        _userPolls.value = newUserPollsList

        PollRepository.deletePoll(context, pollId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPollDeleted.postValue(Constants.API_SUCCESS)
            }

            override fun onError(response: String) {
                _isPollDeleted.postValue(Constants.API_FAILED)

                if (deletedPoll != null) {
                    //re-adding poll when error occurs
                    newPollsList = _allPolls.value!!.toMutableList()
                    newPollsList.add(deletedPollIndex, deletedPoll)
                    _allPolls.value = newPollsList
                }
                newUserPollsList = _userPolls.value!!.toMutableList()
                newUserPollsList.add(deletedUserPollIndex, deletedUserPoll)
                _userPolls.value = newUserPollsList
            }
        })
    }
}