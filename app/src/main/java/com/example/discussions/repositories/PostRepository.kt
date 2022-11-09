package com.example.discussions.repositories

import android.content.Context
import com.example.discussions.api.ResponseCallback
import com.example.discussions.api.apiCalls.post.CreatePostApi
import com.example.discussions.api.apiCalls.post.GetAllPostsApi
import com.example.discussions.models.PostModel
import com.example.discussions.store.LoginStore

class PostRepository {
    companion object {
        var postsList = mutableListOf<PostModel>()

        fun createPost(
            context: Context,
            postTitle: String,
            postContent: String,
            postImage: String?,
            allowComments: Boolean,
            callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            CreatePostApi.createPost(
                context,
                token,
                postTitle,
                postContent,
                postImage,
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

        fun getAllPosts(
            context: Context,
            callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            GetAllPostsApi.getAllPostsJson(
                context,
                token,
                object : ResponseCallback {
                    override fun onSuccess(response: String) {
                        postsList = GetAllPostsApi.parseAllPostsJson(response)
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