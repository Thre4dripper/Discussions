package com.example.discussions.repositories

import android.content.Context
import com.example.discussions.api.ResponseCallback
import com.example.discussions.api.apiCalls.poll.CreatePollApi
import com.example.discussions.models.PollOptionModel
import com.example.discussions.store.LoginStore

class PollRepository {
    companion object {
        private const val TAG = "PollRepository"

        fun createPoll(
            context: Context,
            pollTitle: String,
            pollContent: String,
            pollOptions: List<PollOptionModel>,
            isPrivate: Boolean,
            allowComments: Boolean,
            callback: ResponseCallback
        ) {

            val token = LoginStore.getJWTToken(context)!!

            CreatePollApi.createPoll(
                context,
                token,
                pollTitle,
                pollContent,
                pollOptions,
                isPrivate,
                allowComments,
                object : ResponseCallback {
                    override fun onSuccess(response: String) {
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
                }
            )
        }
    }
}