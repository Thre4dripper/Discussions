package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.ProfileDataModel
import com.example.discussions.repositories.PollRepository
import com.example.discussions.repositories.PostRepository
import com.example.discussions.repositories.UserRepository

class HomeViewModel : ViewModel() {
    private val TAG = "HomeViewModel"

    lateinit var profileDataModel: ProfileDataModel

    //get posts list directly from repository live data
    var postsList = PostRepository.allPostsList

    //get user posts list directly from repository live data
    var userPostsList = PostRepository.userPostsList

    //get user polls list directly from repository live data
    var userPollsList = PollRepository.userPollsList

    private var _isPostsFetched = MutableLiveData<String?>(null)
    val isPostsFetched: LiveData<String?>
        get() = _isPostsFetched

    private var _isProfileFetched = MutableLiveData<String?>(null)
    val isProfileFetched: LiveData<String?>
        get() = _isProfileFetched

    private var _isUserPostsFetched = MutableLiveData<String?>(null)
    val isUserPostsFetched: LiveData<String?>
        get() = _isUserPostsFetched

    private var _isUserPollsFetched = MutableLiveData<String?>(null)
    val isUserPollsFetched: LiveData<String?>
        get() = _isUserPollsFetched

    private var _isPollVoted = MutableLiveData<String?>(null)
    val isPollVoted: LiveData<String?>
        get() = _isPollVoted

    private var _isPostLikedChanged = MutableLiveData<String?>(null)
    val isPostLikedChanged: LiveData<String?>
        get() = _isPostLikedChanged

    private var _isPollLikedChanged = MutableLiveData<String?>(null)
    val isPollLikedChanged: LiveData<String?>
        get() = _isPollLikedChanged

    companion object {
        var postsOrPollsScrollToTop = false
    }


    fun getProfile(context: Context) {
        if (_isProfileFetched.value == Constants.API_SUCCESS)
            return
        else
            _isProfileFetched.value = null


        UserRepository.getProfile(context, object : ResponseCallback {
            override fun onSuccess(response: String) {
                profileDataModel = UserRepository.profileDataModel!!
                _isProfileFetched.postValue(Constants.API_SUCCESS)
            }

            override fun onError(response: String) {
                _isProfileFetched.postValue(response)
            }
        })
    }

    fun getAllUserPosts(context: Context) {
        PostRepository.getAllUserPosts(context, profileDataModel.userId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isUserPostsFetched.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isUserPostsFetched.value = response
                userPostsList.value = mutableListOf()
            }
        })
    }

    fun refreshProfile(context: Context) {
        _isProfileFetched = MutableLiveData<String?>(null)
        _isUserPostsFetched.value = null
        userPostsList.value = null
        getAllUserPosts(context)
    }

    fun getAllPosts(context: Context) {
        if (_isPostsFetched.value == Constants.API_SUCCESS)
            return
        else {
            postsList.value = null
            _isPostsFetched.value = null
        }

        PostRepository.getAllPosts(context, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPostsFetched.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isPostsFetched.value = response
                postsList.value = mutableListOf()
            }
        })
    }

    fun refreshAllPosts(context: Context) {
        postsList.value = null
        _isPostsFetched.value = null
        getAllPosts(context)
    }

    fun getAllUserPolls(context: Context) {
        if (_isUserPollsFetched.value == Constants.API_SUCCESS)
            return
        else {
            userPollsList.value = null
            _isUserPollsFetched.value = null
        }
        postsOrPollsScrollToTop = true

        PollRepository.getAllUserPolls(context, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isUserPollsFetched.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isUserPollsFetched.value = response
                userPollsList.value = mutableListOf()
            }
        })
    }

    fun refreshAllUserPolls(context: Context) {
        userPollsList.value = null
        _isUserPollsFetched.value = null
        getAllUserPolls(context)
    }

    fun pollVote(context: Context, pollId: String, optionId: String) {
        _isPollVoted.value = null
        postsOrPollsScrollToTop = false

        //changing vote status to voting, this will trigger progress bar in recycler view
        val newPollsList = userPollsList.value!!.toMutableList()
        val pollIndex = newPollsList.indexOfFirst { it.pollId == pollId }

        val votedPoll = newPollsList[pollIndex].copy(isVoting = true)
        newPollsList[pollIndex] = votedPoll
        userPollsList.value = newPollsList

        PollRepository.pollVote(context, pollId, optionId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPollVoted.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isPollVoted.value = Constants.API_FAILED
            }
        })
    }

    fun likePost(context: Context, postId: String) {
        _isPostLikedChanged.value = null
        postsOrPollsScrollToTop = false

        //changing like status to liking, this will trigger refresh in recycler view
        var newPostsList = postsList.value!!.toMutableList()
        var postIndex = newPostsList.indexOfFirst { it.postId == postId }
        var post = newPostsList[postIndex].copy(
            isLiked = !newPostsList[postIndex].isLiked,
            likes = newPostsList[postIndex].likes + if (!newPostsList[postIndex].isLiked) 1 else -1
        )
        newPostsList[postIndex] = post
        postsList.value = newPostsList

        PostRepository.likePost(context, postId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPostLikedChanged.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isPostLikedChanged.value = Constants.API_FAILED

                //reverting back to previous state
                newPostsList = postsList.value!!.toMutableList()
                postIndex = newPostsList.indexOfFirst { it.postId == postId }
                post = newPostsList[postIndex].copy(
                    isLiked = !newPostsList[postIndex].isLiked,
                    likes = newPostsList[postIndex].likes + if (!newPostsList[postIndex].isLiked) 1 else -1
                )
                newPostsList[postIndex] = post
                postsList.value = newPostsList
            }
        })
    }

    fun likePoll(context: Context, pollId: String) {
        _isPollLikedChanged.value = null
        postsOrPollsScrollToTop = false

        //changing like status to liking, this will trigger refresh in recycler view
        var newPollsList = userPollsList.value!!.toMutableList()
        var pollIndex = newPollsList.indexOfFirst { it.pollId == pollId }
        var poll = newPollsList[pollIndex].copy(
            isLiked = !newPollsList[pollIndex].isLiked,
            likes = newPollsList[pollIndex].likes + if (!newPollsList[pollIndex].isLiked) 1 else -1
        )
        newPollsList[pollIndex] = poll
        userPollsList.value = newPollsList

        PollRepository.likePoll(context, pollId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPollLikedChanged.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isPollLikedChanged.value = Constants.API_FAILED

                //reverting back to previous state
                newPollsList = userPollsList.value!!.toMutableList()
                pollIndex = newPollsList.indexOfFirst { it.pollId == pollId }
                poll = newPollsList[pollIndex].copy(
                    isLiked = !newPollsList[pollIndex].isLiked,
                    likes = newPollsList[pollIndex].likes + if (!newPollsList[pollIndex].isLiked) 1 else -1
                )
                newPollsList[pollIndex] = poll
                userPollsList.value = newPollsList
            }
        })
    }
}