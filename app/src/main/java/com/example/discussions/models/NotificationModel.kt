package com.example.discussions.models

data class NotificationModel(
    val notificationId: String,
    val type: String,
    val isRead: Boolean,
    val notifierName: String,
    val notifierImage: String,
    val createdAt: String,
    val postId: String?,
    val pollId: String?,
    val commentId: String?
)