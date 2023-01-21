package com.example.discussions.repositories

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.discussions.api.ResponseCallback
import com.example.discussions.api.apiCalls.comments.GetCommentsApi
import com.example.discussions.models.CommentsModel
import com.example.discussions.store.LoginStore

class CommentsRepository {
    companion object {
        val commentsList = MutableLiveData<MutableList<CommentsModel>?>(null)

        fun getAllComments(
            context: Context, postId: Int?, pollId: Int?, callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            GetCommentsApi.getCommentsJson(
                context,
                token,
                postId,
                pollId,
                object : ResponseCallback {
                    override fun onSuccess(response: String) {
                        commentsList.postValue(GetCommentsApi.parseCommentsJson(response))
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
    }
}