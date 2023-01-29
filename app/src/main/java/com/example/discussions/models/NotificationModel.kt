package com.example.discussions.models

data class NotificationModel(
    val notificationId: String,
    val type: String,
    val isRead: Boolean,
    val notifierName: String,
    val notifierImage: String,
    val createdAt: String,
    val post: PostNotificationModel?,
    val poll: PollNotificationModel?,
    val comment: CommentNotificationModel?
)

data class PostNotificationModel(
    val id: String,
    val title: String,
    val content: String,
    val userName: String,
    val userImage: String,
    val createdAt: String,
    val postImage: String,
    val postComment: String?,
)

data class PollNotificationModel(
    val id: String,
    val title: String,
    val content: String,
    val userName: String,
    val userImage: String,
    val createdAt: String,
    val pollComment: String?,
)

data class CommentNotificationModel(
    val id: String,
    val content: String,
    val userName: String,
    val userImage: String,
    val createdAt: String,
    val comment: String?,
)