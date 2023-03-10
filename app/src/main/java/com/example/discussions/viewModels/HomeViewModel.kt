package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.ProfileDataModel
import com.example.discussions.repositories.*

class HomeViewModel : ViewModel() {
    private val TAG = "HomeViewModel"

    lateinit var profileDataModel: ProfileDataModel

    var discussions = DiscussionRepository.discussions

    //get user posts list directly from repository live data
    var userPostsList = PostRepository.userPostsList

    //get user polls list directly from repository live data
    var userPollsList = PollRepository.userPollsList

    //get notifications list directly from repository live data
    var notificationsList = NotificationRepository.notificationsList

    private var _isDiscussionsFetched = MutableLiveData<String?>(null)
    val isDiscussionsFetched: LiveData<String?>
        get() = _isDiscussionsFetched

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

    private var _isNotificationsFetched = MutableLiveData<String?>(null)
    val isNotificationsFetched: LiveData<String?>
        get() = _isNotificationsFetched

    private var _isNotificationDeleted = MutableLiveData<String?>(null)
    val isNotificationDeleted: LiveData<String?>
        get() = _isNotificationDeleted

    private var _isAllNotificationsDeleted = MutableLiveData<String?>(null)
    val isAllNotificationsDeleted: LiveData<String?>
        get() = _isAllNotificationsDeleted

    private var _isNotificationRead = MutableLiveData<String?>(null)
    val isNotificationRead: LiveData<String?>
        get() = _isNotificationRead

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
        var postsOrPollsOrNotificationsScrollToTop = false
    }

    fun getAllDiscussions(context: Context, page: Int) {
        if (_isDiscussionsFetched.value == Constants.API_SUCCESS) return
        else _isDiscussionsFetched.value = null

        DiscussionRepository.getAllDiscussions(context, page, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isDiscussionsFetched.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isDiscussionsFetched.value = response
                discussions.value = mutableListOf()
            }
        })
    }

    fun refreshAllDiscussions(context: Context) {
        _isDiscussionsFetched.value = null
        getAllDiscussions(context, 1)
    }

    fun getProfile(context: Context) {
        if (_isProfileFetched.value == Constants.API_SUCCESS) return
        else _isProfileFetched.value = null


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
        getAllUserPosts(context)
    }

    fun getAllUserPolls(context: Context) {
        if (_isUserPollsFetched.value == Constants.API_SUCCESS) return
        else {
            _isUserPollsFetched.value = null
        }
        postsOrPollsOrNotificationsScrollToTop = true

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
        _isUserPollsFetched.value = null
        getAllUserPolls(context)
    }

    fun pollVote(context: Context, pollId: String, optionId: String) {
        _isPollVoted.value = null
        postsOrPollsOrNotificationsScrollToTop = false

        //changing vote status to voting, this will trigger progress bar in recycler view
        val newAllPollsList = discussions.value?.toMutableList()
        val allPollsIndex = newAllPollsList?.indexOfFirst { it.poll?.pollId == pollId }

        val newUserPollsList = userPollsList.value?.toMutableList()
        val userPollIndex = newUserPollsList?.indexOfFirst { it.poll?.pollId == pollId }

        if (allPollsIndex != null && allPollsIndex != -1) {
            val votedPoll = newAllPollsList[allPollsIndex].poll!!.copy(isVoting = true)
            val newDiscussionPoll = newAllPollsList[allPollsIndex].copy(poll = votedPoll)
            newAllPollsList[allPollsIndex] = newDiscussionPoll
            discussions.value = newAllPollsList
        }

        if (userPollIndex != null && userPollIndex != -1) {
            val votedPoll = newUserPollsList[userPollIndex].poll!!.copy(isVoting = true)
            val newDiscussionPoll = newUserPollsList[userPollIndex].copy(poll = votedPoll)
            newUserPollsList[userPollIndex] = newDiscussionPoll
            userPollsList.value = newUserPollsList
        }

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
        postsOrPollsOrNotificationsScrollToTop = false

        PostRepository.likePost(context, postId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPostLikedChanged.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isPostLikedChanged.value = Constants.API_FAILED
            }
        })
    }

    fun likePoll(context: Context, pollId: String) {
        _isPollLikedChanged.value = null
        postsOrPollsOrNotificationsScrollToTop = false

        PollRepository.likePoll(context, pollId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPollLikedChanged.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isPollLikedChanged.value = Constants.API_FAILED
            }
        })
    }

    fun getAllNotifications(context: Context) {
        if (_isNotificationsFetched.value == Constants.API_SUCCESS) return
        else {
            _isNotificationsFetched.value = null
        }
        postsOrPollsOrNotificationsScrollToTop = true

        NotificationRepository.getAllNotifications(context, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isNotificationsFetched.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isNotificationsFetched.value = response
                notificationsList.value = mutableListOf()
            }
        })
    }

    fun refreshAllNotifications(context: Context) {
        _isNotificationsFetched.value = null
        getAllNotifications(context)
    }

    fun deleteNotificationById(context: Context, notificationId: String) {
        _isNotificationDeleted.value = null
        postsOrPollsOrNotificationsScrollToTop = false

        val oldNotificationsList = notificationsList.value!!.toMutableList()
        val notificationIndex =
            oldNotificationsList.indexOfFirst { it.notificationId == notificationId }
        oldNotificationsList.removeAt(notificationIndex)
        notificationsList.value = oldNotificationsList

        NotificationRepository.deleteNotificationById(
            context,
            notificationId,
            object : ResponseCallback {
                override fun onSuccess(response: String) {
                    _isNotificationDeleted.value = Constants.API_SUCCESS
                }

                override fun onError(response: String) {
                    _isNotificationDeleted.value = Constants.API_FAILED

                    //if notification delete failed, then add it back to list
                    notificationsList.value = oldNotificationsList
                }
            })
    }

    fun deleteAllNotifications(context: Context) {
        _isAllNotificationsDeleted.value = null
        postsOrPollsOrNotificationsScrollToTop = false

        val oldNotificationsList = notificationsList.value!!.toMutableList()
        notificationsList.value = mutableListOf()

        NotificationRepository.deleteAllNotifications(context, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isAllNotificationsDeleted.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isAllNotificationsDeleted.value = Constants.API_FAILED

                //if all notifications delete failed, then add it back to list
                notificationsList.value = oldNotificationsList
            }
        })
    }

    fun readNotification(context: Context, notificationId: String) {
        _isNotificationRead.value = null
        postsOrPollsOrNotificationsScrollToTop = false

        val oldNotificationsList = notificationsList.value!!
        val newNotificationsList = notificationsList.value!!.toMutableList()
        val notificationIndex =
            newNotificationsList.indexOfFirst { it.notificationId == notificationId }

        val readNotification = newNotificationsList[notificationIndex].copy(isRead = true)
        newNotificationsList[notificationIndex] = readNotification
        notificationsList.value = newNotificationsList

        NotificationRepository.readNotification(context, notificationId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isNotificationRead.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                _isNotificationRead.value = Constants.API_FAILED

                //if notification read failed, then add it back to list
                notificationsList.value = oldNotificationsList
            }
        })
    }
}