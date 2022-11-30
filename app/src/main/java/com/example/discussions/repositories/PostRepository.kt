package com.example.discussions.repositories

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.discussions.api.ResponseCallback
import com.example.discussions.api.apiCalls.post.*
import com.example.discussions.models.PostModel
import com.example.discussions.store.LoginStore

class PostRepository {
    companion object {
        private const val TAG = "PostRepository"

        var allPostsList = MutableLiveData<MutableList<PostModel>?>(null)
        var userPostsList = MutableLiveData<MutableList<PostModel>?>(null)

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
                        allPostsList.postValue(GetAllPostsApi.parseAllPostsJson(response))
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

        fun getAllUserPosts(
            context: Context,
            userId: String,
            callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            GetUserPostsApi.getUserPostsJson(
                context,
                userId,
                token,
                object : ResponseCallback {
                    override fun onSuccess(response: String) {
                        userPostsList.postValue(GetUserPostsApi.parseUserPostsJson(response))
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

        fun updatePost(
            context: Context,
            postId: String,
            postTitle: String,
            postContent: String,
            postImage: String?,
            allowComments: Boolean,
            callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            EditPostApi.updatePost(
                context,
                token,
                postId,
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

        fun deletePost(
            context: Context,
            postId: String,
            callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            DeletePostApi.deletePost(
                context,
                postId,
                token,
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

        fun likePost(
            context: Context,
            postId: String,
            callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            PostLikeApi.likePost(
                context,
                postId,
                token,
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