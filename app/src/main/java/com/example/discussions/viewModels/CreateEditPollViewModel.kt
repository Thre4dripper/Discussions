package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.PollOptionModel
import com.example.discussions.repositories.UserRepository

class CreateEditPollViewModel : ViewModel() {

    var profileImage: String? = null
    var username: String = ""
    var pollTitle: String = ""
    var pollContent: String = ""
    var isPrivate: Boolean = true

    val pollOptions =
        MutableLiveData<MutableList<PollOptionModel>>(mutableListOf()) //list of poll options

    private var _isApiFetched = MutableLiveData<String>(null)
    val isApiFetched: LiveData<String>
        get() = _isApiFetched

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

    fun addPollOption() {
        val newPollOptionsList = pollOptions.value!!.toMutableList()
        newPollOptionsList.add(
            PollOptionModel(pollOptions.value!!.size, "", "Option ${pollOptions.value!!.size + 1}")
        )
        pollOptions.postValue(newPollOptionsList)
    }
}