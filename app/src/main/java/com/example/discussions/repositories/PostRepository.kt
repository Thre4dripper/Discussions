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
        var hasMorePosts = MutableLiveData(true)

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
            context: Context, userId: String, page: Int, callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            GetUserPostsApi.getUserPostsJson(
                context,
                userId,
                token,
                page,
                object : ResponseCallback {
                    override fun onSuccess(response: String) {
                        val newUserPostsList = GetUserPostsApi.parseUserPostsJson(response)
                        val oldUserPostsList = userPostsList.value ?: mutableListOf()
                        val updatedUserPostsList = oldUserPostsList.toMutableList()
                        updatedUserPostsList.addAll(newUserPostsList)

                        userPostsList.value = updatedUserPostsList
                        hasMorePosts.value = newUserPostsList.isNotEmpty()
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

        fun cancelGetRequest() {
            GetUserPostsApi.cancelGetRequest()
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

            val oldDiscussionsPostsList = DiscussionRepository.discussions.value
            val oldUserPostsList = userPostsList.value

            val deletedDiscussionPostList = deletePostInData(oldDiscussionsPostsList, postId)
            DiscussionRepository.discussions.postValue(deletedDiscussionPostList)

            val deletedUserPostsList = deletePostInData(oldUserPostsList, postId)
            userPostsList.postValue(deletedUserPostsList)

            DeletePostApi.deletePost(context, postId, token, object : ResponseCallback {
                override fun onSuccess(response: String) {
                    callback.onSuccess(response)
                }

                override fun onError(response: String) {
                    // Revert the changes if the delete post api fails
                    DiscussionRepository.discussions.postValue(oldDiscussionsPostsList)
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

            val oldDiscussionsPostsList = DiscussionRepository.discussions.value
            val oldUserPostsList = userPostsList.value

            val likedDiscussionPostList = likePostInData(oldDiscussionsPostsList, postId)
            DiscussionRepository.discussions.postValue(likedDiscussionPostList)

            val likedUserPostsList = likePostInData(oldUserPostsList, postId)
            userPostsList.postValue(likedUserPostsList)

            PostLikeApi.likePost(context, postId, token, object : ResponseCallback {
                override fun onSuccess(response: String) {
                    callback.onSuccess(response)
                }

                override fun onError(response: String) {
                    // Revert the changes
                    DiscussionRepository.discussions.postValue(oldDiscussionsPostsList)
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

        private fun deletePostInData(
            postsList: MutableList<DiscussionModel>?, postId: String
        ): MutableList<DiscussionModel>? {
            val deletedPost = postsList?.find { it.post?.postId == postId }
            val deletedPostIndex: Int
            val newPostsList: MutableList<DiscussionModel>?

            return if (deletedPost != null) {
                deletedPostIndex = postsList.indexOf(deletedPost)
                newPostsList = postsList.toMutableList()
                newPostsList.removeAt(deletedPostIndex)

                newPostsList
            } else {
                postsList
            }
        }
    }
}