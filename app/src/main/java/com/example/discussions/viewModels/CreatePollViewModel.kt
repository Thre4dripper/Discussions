package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.PollOptionModel
import com.example.discussions.repositories.PollRepository
import com.example.discussions.repositories.UserRepository
import java.util.*

class CreatePollViewModel : ViewModel() {
    private val TAG = "CreatePollViewModel"

    var profileImage: String? = null
    var username: String = ""
    var pollTitle: String = ""
    var pollContent: String = ""
    var isPrivate: Boolean = false
    var allowComments: Boolean = true

    val pollOptions =
        MutableLiveData<MutableList<PollOptionModel>>(mutableListOf()) //list of poll options

    private var _isApiFetched = MutableLiveData<String>(null)
    val isApiFetched: LiveData<String>
        get() = _isApiFetched

    private var _isPollCreated = MutableLiveData<String?>(null)
    val isPollCreated: LiveData<String?>
        get() = _isPollCreated

    fun getUsernameAndImage(context: Context) {
        UserRepository.getUsernameAndImage(context, object : ResponseCallback {
            override fun onSuccess(response: String) {
                UserRepository.map[Constants.PROFILE_IMAGE]?.let { profileImage = it }
                UserRepository.map[Constants.USERNAME]?.let { username = it }
                _isApiFetched.postValue(Constants.API_SUCCESS)
            }

            override fun onError(response: String) {
                _isApiFetched.postValue(response)
            }
        })
    }

    /**
     * METHOD FOR ADDING POLL OPTION WITH UNIQUE ID
     */
    fun addPollOption() {
        val newPollOptionsList = pollOptions.value!!.toMutableList()
        newPollOptionsList.add(
            PollOptionModel(
                UUID.randomUUID().toString(),
                "",
                "Option ${pollOptions.value!!.size + 1}"
            )
        )
        pollOptions.postValue(newPollOptionsList)
    }

    /**
     * METHOD FOR REMOVING POLL OPTION
     */
    fun deletePollOption(position: Int) {
        //new list of poll options
        val newPollOptionsList = mutableListOf<PollOptionModel>()

        var itr = 1
        //adding all options except the one to be deleted
        pollOptions.value!!.forEachIndexed { index, pollOptionModel ->
            if (index != position) {
                newPollOptionsList.add(
                    PollOptionModel(
                        pollOptionModel.id,
                        pollOptionModel.content,
                        //hint will be in sequence
                        "Option ${itr++}"
                    )
                )
            }
        }

        pollOptions.postValue(newPollOptionsList)
    }

    /**
     * METHOD FOR SAVING POLL OPTION TEXT IN LIST
     */
    fun updatePollOption(position: Int, text: String) {
        val newPollOptionsList = pollOptions.value!!.toMutableList()
        newPollOptionsList[position].content = text
        pollOptions.postValue(newPollOptionsList)
    }

    fun createPoll(context: Context) {
        _isPollCreated.postValue(null)
        PollRepository.createPoll(
            context,
            pollTitle,
            pollContent,
            pollOptions.value!!,
            isPrivate,
            allowComments,
            object : ResponseCallback {
                override fun onSuccess(response: String) {
                    _isPollCreated.postValue(Constants.API_SUCCESS)
                }

                override fun onError(response: String) {
                    _isPollCreated.postValue(response)
                }
            })
    }
}