package com.example.discussions.adapters.interfaces

import com.example.discussions.models.NotificationModel

interface NotificationInterface {
    fun onNotificationClick(notification: NotificationModel)
    fun onNotificationOptionsClick(notification: NotificationModel)
    fun onNotificationDelete(notificationId: String)
    fun onNotificationMarkAsRead(notificationId: String)
}