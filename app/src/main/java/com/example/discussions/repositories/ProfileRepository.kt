package com.example.discussions.repositories

import android.content.Context
import android.util.Log
import com.example.discussions.api.ResponseCallback
import com.example.discussions.api.apiCalls.ProfileApi
import com.example.discussions.store.LoginStore

class ProfileRepository {
    companion object {
        private const val TAG = "ProfileRepository"
        var map = mapOf<String, String>()

        fun getProfile(
            context: Context, callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!
            ProfileApi.getProfileJson(context, token, object : ResponseCallback {
                override fun onSuccess(response: String) {
                    map = ProfileApi.parseProfileJson(response)
                    Log.d(TAG, "onSuccess: $map")
                    callback.onSuccess("Success")
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