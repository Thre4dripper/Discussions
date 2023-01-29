package com.example.discussions.adapters.interfaces

interface NotificationInterface {
    fun onNotificationClick(notificationId: String)
    fun onNotificationDelete(notificationId: String)
    fun onNotificationMarkAsRead(notificationId: String)
}