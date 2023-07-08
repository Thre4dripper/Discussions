package com.example.discussions.viewModels

import android.content.Context
import android.widget.Toast
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


    /**
     * PAGINATION STUFF
     */
    private var discussionsPage = 0
    val hasMoreDiscussions = DiscussionRepository.hasMoreDiscussions

    private var _paginationStatus = MutableLiveData(Constants.PAGE_IDLE)
    val paginationStatus: LiveData<String?>
        get() = _paginationStatus

    companion object {
        var discussionsScrollToTop = false
    }

    fun getAllDiscussions(context: Context) {
        _isDiscussionsFetched.value = null
        _paginationStatus.value = Constants.PAGE_LOADING

        discussionsPage++
        DiscussionRepository.getAllDiscussions(context, discussionsPage, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isDiscussionsFetched.value = Constants.API_SUCCESS
                _paginationStatus.value = Constants.PAGE_IDLE
            }

            override fun onError(response: String) {
                _isDiscussionsFetched.value = response
                val alternateList = discussions.value?.toMutableList() ?: mutableListOf()
                discussions.value = alternateList
                _paginationStatus.value = Constants.PAGE_IDLE

                Toast.makeText(context, response, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun refreshAllDiscussions(context: Context) {
        DiscussionRepository.cancelGetRequest()
        DiscussionRepository.discussions.value = null
        discussionsPage = 0
        _paginationStatus.value = Constants.PAGE_IDLE
        getAllDiscussions(context)
    }
}