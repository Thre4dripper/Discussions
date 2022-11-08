package com.example.discussions.repositories

import android.content.Context
import com.example.discussions.api.ResponseCallback
import com.example.discussions.api.apiCalls.user.DetailsApi
import com.example.discussions.api.apiCalls.user.ProfileApi
import com.example.discussions.models.ProfileDataModel
import com.example.discussions.store.LoginStore

class UserRepository {
    companion object {
        private const val TAG = "ProfileRepository"
        var map = mapOf<String, String>()
        var profileDataModel: ProfileDataModel? = null

        fun getDetails(
            context: Context, callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!
            DetailsApi.getDetailsJson(context, token, object : ResponseCallback {
                override fun onSuccess(response: String) {
                    map = DetailsApi.parseDetailsJson(response)
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

        fun updateDetails(
            context: Context,
            imageUrl: String,
            username: String,
            firstName: String,
            lastName: String,
            gender: String,
            email: String,
            mobileNo: String,
            dob: String,
            address: String,
            callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!
            DetailsApi.updateDetails(context,
                token,
                imageUrl,
                username,
                firstName,
                lastName,
                gender,
                email,
                mobileNo,
                dob,
                address,
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
                })
        }

        fun getProfile(
            context: Context, callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!
            ProfileApi.getProfileJson(context, token, object : ResponseCallback {
                override fun onSuccess(response: String) {
                    profileDataModel = ProfileApi.parseProfileJson(response)
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