package com.example.discussions.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.ui.PostDetailsActivity
import org.json.JSONObject

class PostNotifications {
    companion object {
        private const val TAG = "PostNotifications"
        fun likeNotification(context: Context, data: JSONObject) {
            val notifier = data.getJSONObject("created_by").getString("username")
            val notifierImage = data.getJSONObject("created_by").getString("image")
            val postId = data.getJSONObject("post").getString("id")
            val postImage = data.getJSONObject("post").getString("image")
            val postTitle = data.getJSONObject("post").getString("title")
            val postContent = data.getJSONObject("post").getString("content")

            val notificationId = ("${Constants.POST_LIKE_NOTIFICATION_ID}$postId").toInt()
            val notificationTitle = "$notifier liked your post"
            val notificationContent =
                if (postTitle.isEmpty() && postContent.isEmpty()) "Tap to view post" else "$postTitle $postContent"
            val notificationUserImage = FCMConfig.getBitmapFromUrl(notifierImage)
            val notificationPostImage = FCMConfig.getBitmapFromUrl(postImage)

            val intent = Intent(context, PostDetailsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            intent.putExtra(Constants.POST_ID, postId)
            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(
                    context,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )

            val builder = NotificationCompat.Builder(context, Constants.POST_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationTitle)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentText(notificationContent)
                .setLargeIcon(notificationUserImage)
                .setStyle(
                    NotificationCompat.BigPictureStyle().bigPicture(notificationPostImage)
                )
                .setContentIntent(pendingIntent).setAutoCancel(true).build()

            with(NotificationManagerCompat.from(context)) {
                try {
                    notify(notificationId, builder)
                } catch (e: SecurityException) {
                    Log.d(TAG, "e: $e")
                }
            }
        }

        fun commentNotification(context: Context, data: JSONObject) {
            val notifier = data.getJSONObject("created_by").getString("username")
            val notifierImage = data.getJSONObject("created_by").getString("image")
            val postId = data.getJSONObject("post").getString("id")
            val postTitle = data.getJSONObject("post").getString("title")
            val postContent = data.getJSONObject("post").getString("content")
            val postComment = data.getJSONObject("post").getString("comment")

            val notificationId = ("${Constants.POST_COMMENT_NOTIFICATION_ID}$postId").toInt()
            val notificationTitle = "$notifier commented on your post"
            val notificationContent =
                if (postTitle.isEmpty() && postContent.isEmpty()) "Tap to view post"
                else "$postTitle $postContent"

            val notificationUserImage = FCMConfig.getBitmapFromUrl(notifierImage)

            val intent = Intent(context, PostDetailsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            intent.putExtra(Constants.POST_ID, postId)
            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(
                    context,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )

            val builder = NotificationCompat.Builder(context, Constants.POST_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationTitle)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentText(notificationContent)
                .setLargeIcon(notificationUserImage)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("$notificationContent \n\n$postComment")
                )
                .setContentIntent(pendingIntent).setAutoCancel(true).build()

            with(NotificationManagerCompat.from(context)) {
                try {
                    notify(notificationId, builder)
                } catch (e: SecurityException) {
                    Log.d(TAG, "e: $e")
                }
            }
        }
    }
}