package com.example.discussions.repositories

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.discussions.api.ResponseCallback
import com.example.discussions.api.apiCalls.discussion.GetAllDiscussionsApi
import com.example.discussions.models.DiscussionModel
import com.example.discussions.store.LoginStore

class DiscussionRepository {
    companion object {
        private const val TAG = "DiscussionRepository"

        val discussions = MutableLiveData<MutableList<DiscussionModel>?>(null)
        val hasMoreDiscussions = MutableLiveData(false)

        fun getAllDiscussions(
            context: Context,
            page: Int,
            callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            GetAllDiscussionsApi.getAllDiscussionsJson(
                context,
                token,
                page,
                object : ResponseCallback {
                    override fun onSuccess(response: String) {
                        val newDiscussionsList =
                            GetAllDiscussionsApi.parseAllDiscussionsJson(response)
                        val oldDiscussionsList = discussions.value ?: mutableListOf()
                        val updatedDiscussionsList = oldDiscussionsList.toMutableList()
                        updatedDiscussionsList.addAll(newDiscussionsList)
                        discussions.value = updatedDiscussionsList
                        hasMoreDiscussions.value =
                            updatedDiscussionsList.lastOrNull()?.next != null
                        callback.onSuccess(response)
                    }

                    override fun onError(response: String) {
                        if (response.contains("com.android.volley.TimeoutError")) {
                            callback.onError("Time Out")
                        } else if (response.contains("com.android.volley.NoConnectionError")) {
                            callback.onError("Please check your internet connection")
                        } else if (response.contains("com.android.volley.AuthFailureError")) {
                            callback.onError("Auth Error")
                        } else if (response.contains("com.android.volley.NetworkError")) {
                            callback.onError("Network Error")
                        } else if (response.contains("com.android.volley.ServerError")) {
                            callback.onError("Server Error")
                        } else if (response.contains("com.android.volley.ParseError")) {
                            callback.onError("Parse Error")
                        } else {
                            callback.onError("Something went wrong")
                        }
                    }
                })
        }

        fun cancelAllRequests() {
            GetAllDiscussionsApi.cancelAllRequests()
        }
    }
}