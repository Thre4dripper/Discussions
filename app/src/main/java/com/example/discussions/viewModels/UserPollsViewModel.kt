package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.repositories.PollRepository

class UserPollsViewModel : ViewModel() {
    private val TAG = "UserPollsViewModel"

    //get user list directly from repository live data
    private var _userPolls = PollRepository.userPollsList

    //all polls list from repository
    //TODO remove polls from supreme list containing polls and posts

    private var _isPollDeleted = MutableLiveData<String?>(null)
    val isPollDeleted: LiveData<String?>
        get() = _isPollDeleted

    fun deletePoll(context: Context,pollId: String) {
        _isPollDeleted.value = null
        HomeViewModel.postsOrPollsScrollToTop = false

        //deleting poll from user polls list
        val deletedUserPoll = _userPolls.value!!.find { it.pollId == pollId }!!
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

                //re-adding poll when error occurs
                newUserPollsList = _userPolls.value!!.toMutableList()
                newUserPollsList.add(deletedUserPollIndex, deletedUserPoll)
                _userPolls.value = newUserPollsList
            }
        })
    }
}