package com.example.discussions.repositories

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.discussions.api.ResponseCallback
import com.example.discussions.api.apiCalls.poll.CreatePollApi
import com.example.discussions.api.apiCalls.poll.DeletePollApi
import com.example.discussions.api.apiCalls.poll.GetPollByIdApi
import com.example.discussions.api.apiCalls.poll.GetUserPollsApi
import com.example.discussions.api.apiCalls.poll.PollLikeApi
import com.example.discussions.api.apiCalls.poll.PollVoteApi
import com.example.discussions.models.DiscussionModel
import com.example.discussions.models.PollModel
import com.example.discussions.models.PollOptionModel
import com.example.discussions.store.LoginStore

class PollRepository {
    companion object {
        private const val TAG = "PollRepository"


        val userPollsList = MutableLiveData<MutableList<DiscussionModel>?>(null)
        val singlePoll = MutableLiveData<PollModel?>(null)
        val hasMorePolls = MutableLiveData(false)

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
            context: Context, page: Int, callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            GetUserPollsApi.getUserPollsJson(context, token, page, object : ResponseCallback {
                override fun onSuccess(response: String) {
                    val newUserPollsList = GetUserPollsApi.parseUserPollsJson(response)
                    val oldUserPollsList = userPollsList.value ?: mutableListOf()
                    val updatedUserPollsList = oldUserPollsList.toMutableList()
                    updatedUserPollsList.addAll(newUserPollsList)

                    userPollsList.value = updatedUserPollsList
                    hasMorePolls.value = newUserPollsList.isNotEmpty()
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
            GetUserPollsApi.cancelGetRequest()
        }

        fun pollVote(
            context: Context, pollId: String, pollOptionId: String, callback: ResponseCallback
        ) {
            val token = LoginStore.getJWTToken(context)!!

            //create a new list to update the live data even if this doest not exist
            //single poll is opened from outside and polls list is empty
            val oldAllPollsList =
                DiscussionRepository.discussions.value
                    ?: mutableListOf()

            val oldUserPollsList =
                userPollsList.value?.toMutableList()
                    ?: mutableListOf()

            PollVoteApi.pollVoteJson(
                context,
                token,
                pollId,
                pollOptionId,
                object : ResponseCallback {
                    override fun onSuccess(response: String) {
                        val votedPoll = PollVoteApi.parseVoteJson(response)

                        //single poll live data should be updated on main thread
                        singlePoll.value = votedPoll

                        val newAllPollsList = oldAllPollsList.toMutableList()
                        val newUserPollsList = oldUserPollsList.toMutableList()

                        val allPollIndex =
                            newAllPollsList.indexOfFirst { it.poll?.pollId == pollId }
                        val userPollIndex =
                            newUserPollsList.indexOfFirst { it.poll?.pollId == pollId }

                        //update the polls list
                        if (allPollIndex != -1) {
                            val newDiscussionPoll =
                                newAllPollsList[allPollIndex].copy(poll = votedPoll)
                            newAllPollsList[allPollIndex] = newDiscussionPoll
                            DiscussionRepository.discussions.postValue(newAllPollsList)
                        }

                        //update the user polls list
                        if (userPollIndex != -1) {
                            val newUserPoll = newUserPollsList[userPollIndex].copy(poll = votedPoll)
                            newUserPollsList[userPollIndex] = newUserPoll
                            userPollsList.postValue(newUserPollsList)
                        }

                        callback.onSuccess(response)
                    }

                    override fun onError(response: String) {

                        //revert the changes if the vote was not successful
                        DiscussionRepository.discussions.postValue(oldAllPollsList)
                        userPollsList.postValue(oldUserPollsList)

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

            val oldDiscussionsPollsList = DiscussionRepository.discussions.value
            val oldUserPollsList = userPollsList.value

            val deletedDiscussionPollsList = deletePollInData(oldDiscussionsPollsList, pollId)
            DiscussionRepository.discussions.postValue(deletedDiscussionPollsList)

            val deletedUserPollsList = deletePollInData(oldUserPollsList, pollId)
            userPollsList.postValue(deletedUserPollsList)

            DeletePollApi.deletePoll(context, pollId, token, object : ResponseCallback {
                override fun onSuccess(response: String) {
                    callback.onSuccess(response)
                }

                override fun onError(response: String) {
                    // Revert the changes if the delete poll api fails
                    DiscussionRepository.discussions.postValue(oldDiscussionsPollsList)
                    userPollsList.postValue(oldUserPollsList)

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

        fun getPollByID(context: Context, pollId: String, callback: ResponseCallback) {
            val token = LoginStore.getJWTToken(context)!!

            GetPollByIdApi.getPollByIdJson(context, token, pollId, object : ResponseCallback {
                override fun onSuccess(response: String) {
                    singlePoll.value = GetPollByIdApi.parsePollByIdJson(response)
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

            val oldAllPollsList = DiscussionRepository.discussions.value
            val oldUserPollsList = userPollsList.value

            val updatedAllPollsList = likePollInData(oldAllPollsList, pollId)
            DiscussionRepository.discussions.postValue(updatedAllPollsList)

            val updatedUserPollsList = likePollInData(oldUserPollsList, pollId)
            userPollsList.postValue(updatedUserPollsList)

            PollLikeApi.likePoll(context, pollId, token, object : ResponseCallback {
                override fun onSuccess(response: String) {
                    callback.onSuccess(response)
                }

                override fun onError(response: String) {
                    // Revert the changes
                    DiscussionRepository.discussions.postValue(oldAllPollsList)
                    userPollsList.postValue(oldUserPollsList)

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
            pollsList: MutableList<DiscussionModel>?, pollId: String
        ): MutableList<DiscussionModel>? {
            val likedPoll = pollsList?.find { it.poll?.pollId == pollId }
            val likedPollIndex: Int
            val newPollsList: MutableList<DiscussionModel>?

            return if (likedPoll != null) {
                likedPollIndex = pollsList.indexOf(likedPoll)
                newPollsList = pollsList.toMutableList()
                val poll = likedPoll.poll!!.copy(
                    isLiked = !likedPoll.poll.isLiked,
                    likes = likedPoll.poll.likes + if (!likedPoll.poll.isLiked) 1 else -1
                )

                val discussionPoll = likedPoll.copy(poll = poll)
                newPollsList[likedPollIndex] = discussionPoll

                newPollsList
            } else {
                pollsList
            }
        }

        private fun deletePollInData(
            pollsList: MutableList<DiscussionModel>?, pollId: String
        ): MutableList<DiscussionModel>? {
            val deletedPoll = pollsList?.find { it.poll?.pollId == pollId }
            val deletedPollIndex: Int
            val newPollsList: MutableList<DiscussionModel>?

            return if (deletedPoll != null) {
                deletedPollIndex = pollsList.indexOf(deletedPoll)
                newPollsList = pollsList.toMutableList()
                newPollsList.removeAt(deletedPollIndex)

                newPollsList
            } else {
                pollsList
            }
        }
    }
}