package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.repositories.NotificationRepository

class NotificationsViewModel : ViewModel() {
    //get notifications list directly from repository live data
    var notificationsList = NotificationRepository.notificationsList

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

    /**
     * PAGINATION STUFF
     */
    private var notificationsPage = 0
    var hasMoreNotifications = NotificationRepository.hasMoreNotifications

    private var _paginationStatus = MutableLiveData(Constants.PAGE_IDLE)
    val paginationStatus: LiveData<String?>
        get() = _paginationStatus

    companion object {
        //todo use this instead of HomeViewModel.postsOrPollsOrNotificationsScrollToTop
        var notificationsScrollToIndex = false
    }

    fun getAllNotifications(context: Context) {
        _isNotificationsFetched.value = null
        _paginationStatus.value = Constants.PAGE_LOADING

        notificationsPage++
        NotificationRepository.getAllNotifications(
            context,
            notificationsPage,
            object : ResponseCallback {
                override fun onSuccess(response: String) {
                    _isNotificationsFetched.value = Constants.API_SUCCESS
                    _paginationStatus.value = Constants.PAGE_IDLE
                }

                override fun onError(response: String) {
                    _isNotificationsFetched.value = response
                    notificationsList.value = mutableListOf()
                    _paginationStatus.value = Constants.PAGE_IDLE
                }
            })
    }

    fun refreshAllNotifications(context: Context) {
        NotificationRepository.cancelGetRequest()
        NotificationRepository.notificationsList.value = null
        notificationsPage = 0
        _paginationStatus.value = Constants.PAGE_IDLE
        getAllNotifications(context)
    }

    fun deleteNotificationById(context: Context, notificationId: String) {
        _isNotificationDeleted.value = null
        HomeViewModel.postsOrPollsOrNotificationsScrollToTop = false

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
        HomeViewModel.postsOrPollsOrNotificationsScrollToTop = false

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
        HomeViewModel.postsOrPollsOrNotificationsScrollToTop = false

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