package com.example.discussions.repositories

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.discussions.api.ResponseCallback
import com.example.discussions.api.apiCalls.poll.*
import com.example.discussions.models.PollModel
import com.example.discussions.models.PollOptionModel
import com.example.discussions.store.LoginStore

class PollRepository {
    companion object {
        private const val TAG = "PollRepository"

        val userPollsList = MutableLiveData<MutableList<PollModel>?>(null)

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

            CreatePollApi.createPoll(context,
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
                })
        }

        fun getAllUserPolls(
            context: Context, callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            GetUserPollsApi.getUserPollsJson(context, token, object : ResponseCallback {
                override fun onSuccess(response: String) {
                    userPollsList.postValue(GetUserPollsApi.parseUserPollsJson(response))
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

        fun pollVote(
            context: Context, pollId: String, pollOptionId: String, callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!
            val newPollsList = userPollsList.value!!.toMutableList()

            PollVoteApi.pollVoteJson(
                context,
                token,
                pollId,
                pollOptionId,
                object : ResponseCallback {
                    override fun onSuccess(response: String) {
                        val votedPoll = PollVoteApi.parseVoteJson(response)

                        val pollIndex = newPollsList.indexOfFirst { it.pollId == votedPoll.pollId }

                        newPollsList[pollIndex] = votedPoll
                        userPollsList.postValue(newPollsList)

                        callback.onSuccess(response)
                    }

                    override fun onError(response: String) {

                        //on error simply return the old list by changing voting status to false
                        val pollIndex = newPollsList.indexOfFirst { it.pollId == pollId }

                        val votedPoll = newPollsList[pollIndex].copy(isVoting = false)
                        newPollsList[pollIndex] = votedPoll
                        userPollsList.postValue(newPollsList)

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

        fun deletePoll(
            context: Context, pollId: String, callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            DeletePollApi.deletePoll(context, pollId, token, object : ResponseCallback {
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

        fun likePoll(
            context: Context, pollId: String, callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            val oldPollsList = userPollsList.value!!.toMutableList()
            val updatedPollsList = likePollInData(oldPollsList, pollId)
            userPollsList.postValue(updatedPollsList)

            PollLikeApi.likePoll(context, pollId, token, object : ResponseCallback {
                override fun onSuccess(response: String) {
                    callback.onSuccess(response)
                }

                override fun onError(response: String) {
                    // Revert the changes
                    userPollsList.postValue(oldPollsList)

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

        private fun likePollInData(
            pollsList: MutableList<PollModel>?, pollId: String
        ): MutableList<PollModel>? {
            val likedPoll = pollsList?.find { it.pollId == pollId }
            val likedPollIndex: Int
            var newPollsList: MutableList<PollModel>? = null

            if (likedPoll != null) {
                likedPollIndex = pollsList.indexOf(likedPoll)
                newPollsList = pollsList.toMutableList()
                val poll = likedPoll.copy(
                    isLiked = !likedPoll.isLiked,
                    likes = likedPoll.likes + if (!likedPoll.isLiked) 1 else -1
                )
                newPollsList[likedPollIndex] = poll
            }

            return newPollsList
        }
    }
}