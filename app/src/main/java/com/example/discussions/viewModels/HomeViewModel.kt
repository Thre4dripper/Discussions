package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.repositories.DiscussionRepository

class HomeViewModel : ViewModel() {
    private val TAG = "HomeViewModel"


    var discussions = DiscussionRepository.discussions

    private var _isDiscussionsFetched = MutableLiveData<String?>(null)
    val isDiscussionsFetched: LiveData<String?>
        get() = _isDiscussionsFetched

    companion object {
        //TODO refactor this and make it local to each fragment/list
        var postsOrPollsOrNotificationsScrollToTop = false
    }

    fun getAllDiscussions(context: Context, page: Int) {
        if (_isDiscussionsFetched.value == Constants.API_SUCCESS) return
        else _isDiscussionsFetched.value = null

        DiscussionRepository.getAllDiscussions(context, page, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isDiscussionsFetched.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isDiscussionsFetched.value = response
                discussions.value = mutableListOf()
            }
        })
    }

    fun refreshAllDiscussions(context: Context) {
        _isDiscussionsFetched.value = null
        getAllDiscussions(context, 1)
    }
}