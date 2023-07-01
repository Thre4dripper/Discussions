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
    private var discussionsPage = 0
    var hasMoreDiscussions = DiscussionRepository.hasMoreDiscussions

    private var _isDiscussionsFetched = MutableLiveData<String?>(null)
    val isDiscussionsFetched: LiveData<String?>
        get() = _isDiscussionsFetched

    private var _isLoadingMore = MutableLiveData(Constants.PAGE_IDLE)
    val isLoadingMore: LiveData<String?>
        get() = _isLoadingMore

    companion object {
        //TODO refactor this and make it local to each fragment/list
        var postsOrPollsOrNotificationsScrollToTop = false
    }

    fun getAllDiscussions(context: Context) {
        _isDiscussionsFetched.value = null
        _isLoadingMore.value = Constants.PAGE_LOADING

        discussionsPage++
        DiscussionRepository.getAllDiscussions(context, discussionsPage, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isDiscussionsFetched.value = Constants.API_SUCCESS
                _isLoadingMore.value = Constants.PAGE_IDLE
            }

            override fun onError(response: String) {
                _isDiscussionsFetched.value = response
                discussions.value = mutableListOf()
                _isLoadingMore.value = Constants.PAGE_IDLE
            }
        })
    }

    fun refreshAllDiscussions(context: Context) {
        DiscussionRepository.cancelAllRequests()
        DiscussionRepository.discussions.value = null
        discussionsPage = 0
        getAllDiscussions(context)
    }
}