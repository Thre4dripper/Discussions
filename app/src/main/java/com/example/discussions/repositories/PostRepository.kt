package com.example.discussions.repositories

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.discussions.api.ResponseCallback
import com.example.discussions.api.apiCalls.post.*
import com.example.discussions.models.DiscussionModel
import com.example.discussions.models.PostModel
import com.example.discussions.store.LoginStore

class PostRepository {
    companion object {
        private const val TAG = "PostRepository"

        var userPostsList = MutableLiveData<MutableList<DiscussionModel>?>(null)
        var singlePost = MutableLiveData<PostModel?>(null)

        fun createPost(
            context: Context,
            postTitle: String,
            postContent: String,
            postImage: String?,
            allowComments: Boolean,
            callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            CreatePostApi.createPost(context,
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
                })
        }

        fun getAllUserPosts(
            context: Context, userId: String, callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            GetUserPostsApi.getUserPostsJson(context, userId, token, object : ResponseCallback {
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
            })
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

            EditPostApi.updatePost(context,
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
                })
        }

        fun deletePost(
            context: Context, postId: String, callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            DeletePostApi.deletePost(context, postId, token, object : ResponseCallback {
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

        fun getPostByID(context: Context, postId: String, callback: ResponseCallback) {
            val token = LoginStore.getJWTToken(context)!!

            GetPostByIdApi.getPostByIdJson(context, token, postId, object : ResponseCallback {
                override fun onSuccess(response: String) {
                    singlePost.value = GetPostByIdApi.parsePostByIdJson(response)
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

        fun likePost(
            context: Context, postId: String, callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            val oldAllPostsList = DiscussionRepository.discussions.value
            val oldUserPostsList = userPostsList.value

            val updatedAllPostsList = likePostInData(oldAllPostsList, postId)
            DiscussionRepository.discussions.postValue(updatedAllPostsList)

            val updatedUserPostsList = likePostInData(oldUserPostsList, postId)
            userPostsList.postValue(updatedUserPostsList)

            PostLikeApi.likePost(context, postId, token, object : ResponseCallback {
                override fun onSuccess(response: String) {
                    callback.onSuccess(response)
                }

                override fun onError(response: String) {
                    // Revert the changes
                    DiscussionRepository.discussions.postValue(oldAllPostsList)
                    userPostsList.postValue(oldUserPostsList)

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


        /**
         * METHOD POST DATA MANIPULATION
         */

        private fun likePostInData(
            postsList: MutableList<DiscussionModel>?, postId: String
        ): MutableList<DiscussionModel>? {
            val likedPost = postsList?.find { it.post?.postId == postId }
            val likedPostIndex: Int
            val newPostsList: MutableList<DiscussionModel>?

            return if (likedPost != null) {
                likedPostIndex = postsList.indexOf(likedPost)
                newPostsList = postsList.toMutableList()
                val post = likedPost.post!!.copy(
                    isLiked = !likedPost.post.isLiked,
                    likes = likedPost.post.likes + if (!likedPost.post.isLiked) 1 else -1
                )

                val discussionPost = likedPost.copy(post = post)
                newPostsList[likedPostIndex] = discussionPost

                newPostsList
            } else {
                postsList
            }


        }
    }
}