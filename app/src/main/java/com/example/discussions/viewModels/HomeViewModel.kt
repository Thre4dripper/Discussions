package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Cloudinary
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.DiscussionModel
import com.example.discussions.repositories.DiscussionRepository
import com.example.discussions.repositories.NotificationRepository
import com.example.discussions.repositories.PollRepository
import com.example.discussions.repositories.PostRepository

class HomeViewModel : ViewModel() {
    private val TAG = "HomeViewModel"


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

    private var _isPostDeleted = MutableLiveData<String?>(null)
    val isPostDeleted: LiveData<String?>
        get() = _isPostDeleted


    private var _isUserPollsFetched = MutableLiveData<String?>(null)
    val isUserPollsFetched: LiveData<String?>
        get() = _isUserPollsFetched

    private var _isPollDeleted = MutableLiveData<String?>(null)
    val isPollDeleted: LiveData<String?>
        get() = _isPollDeleted

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


    fun deletePost(context: Context, postId: String) {
        _isPostDeleted.value = null

        //deleting post from all posts list
        val deletedPost = discussions.value!!.find { it.post?.postId == postId }
        var deletedPostIndex = -1
        var newPostsList: MutableList<DiscussionModel>

        //when all posts list is not updated yet after inserting new post then deleted post can only be found in user posts list
        if (deletedPost != null) {
            deletedPostIndex = discussions.value!!.indexOf(deletedPost)
            newPostsList = discussions.value!!.toMutableList()
            newPostsList.removeAt(deletedPostIndex)
            discussions.value = newPostsList
        }

        //deleting post from user posts list
        val deletedUserPost = userPostsList.value?.find { it.post?.postId == postId }
        var deletedUserPostIndex = -1
        var newUserPostsList: MutableList<DiscussionModel>

        if (deletedUserPost != null) {
            deletedUserPostIndex = userPostsList.value!!.indexOf(deletedUserPost)
            newUserPostsList = userPostsList.value!!.toMutableList()
            newUserPostsList.removeAt(deletedUserPostIndex)
            userPostsList.value = newUserPostsList
        }


        PostRepository.deletePost(context, postId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPostDeleted.postValue(Constants.API_SUCCESS)

                val imageUrl =
                    deletedPost?.post?.postImage ?: deletedUserPost?.post?.postImage ?: ""
                if (imageUrl.isNotEmpty()) Cloudinary.deleteImage(context, imageUrl)
            }

            override fun onError(response: String) {
                _isPostDeleted.postValue(Constants.API_FAILED)

                if (deletedPost != null) {
                    //re-adding post when error occurs
                    newPostsList = discussions.value!!.toMutableList()
                    newPostsList.add(deletedPostIndex, deletedPost)
                    discussions.value = newPostsList
                }

                if (deletedUserPost != null) {
                    //re-adding post when error occurs
                    newUserPostsList = userPostsList.value!!.toMutableList()
                    newUserPostsList.add(deletedUserPostIndex, deletedUserPost)
                    userPostsList.value = newUserPostsList
                }
            }
        })
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

    fun deletePoll(context: Context, pollId: String) {
        _isPollDeleted.value = null
        postsOrPollsOrNotificationsScrollToTop = false

        //deleting poll from all polls list
        val deletedPoll = discussions.value?.find { it.poll!!.pollId == pollId }
        var deletedPollIndex = -1
        var newPollsList: MutableList<DiscussionModel>

        //when all polls list is not updated yet after inserting new poll then deleted poll can only be found in user polls list
        if (deletedPoll != null) {
            deletedPollIndex = discussions.value!!.indexOf(deletedPoll)
            newPollsList = discussions.value!!.toMutableList()
            newPollsList.removeAt(deletedPollIndex)
            discussions.value = newPollsList
        }

        //deleting poll from user polls list
        val deletedUserPoll = userPollsList.value?.find { it.poll!!.pollId == pollId }
        var deletedUserPollIndex = -1
        var newUserPollsList: MutableList<DiscussionModel>

        if (deletedUserPoll != null) {
            deletedUserPollIndex = userPollsList.value!!.indexOf(deletedUserPoll)
            newUserPollsList = userPollsList.value!!.toMutableList()
            newUserPollsList.removeAt(deletedUserPollIndex)
            userPollsList.value = newUserPollsList
        }

        PollRepository.deletePoll(context, pollId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPollDeleted.postValue(Constants.API_SUCCESS)
            }

            override fun onError(response: String) {
                _isPollDeleted.postValue(Constants.API_FAILED)

                if (deletedPoll != null) {
                    //re-adding poll when error occurs
                    newPollsList = discussions.value!!.toMutableList()
                    newPollsList.add(deletedPollIndex, deletedPoll)
                    discussions.value = newPollsList
                }

                if (deletedUserPoll != null) {
                    //re-adding poll when error occurs
                    newUserPollsList = userPollsList.value!!.toMutableList()
                    newUserPollsList.add(deletedUserPollIndex, deletedUserPoll)
                    userPollsList.value = newUserPollsList
                }
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