package com.example.discussions.repositories

import android.content.Context
import com.example.discussions.api.ResponseCallback
import com.example.discussions.api.apiCalls.LoginApi
import com.example.discussions.api.apiCalls.SignupApi
import org.json.JSONObject

class AuthRepository {
    companion object {
        private const val TAG = "LoginRepository"

        fun loginUser(
            context: Context, username: String, password: String, callback: ResponseCallback
        ) {
            LoginApi.loginUser(context, username, password, object : ResponseCallback {
                override fun onSuccess(response: String) {
                    //getting jwt token from response
                    val rootObject = JSONObject(response)
                    val token = rootObject.getString("token")
                    callback.onSuccess(token)
                }

                override fun onError(response: String) {
                    if (response.contains("com.android.volley.TimeoutError")) {
                        callback.onError("Time Out")
                    } else if (response.contains("com.android.volley.NoConnectionError")) {
                        callback.onError("Please check your internet connection")
                    } else if (response.contains("com.android.volley.AuthFailureError")) {
                        callback.onError("Invalid Credentials")
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