package com.example.discussions.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.ui.home.HomeActivity
import org.json.JSONObject

class CommentNotifications {
    companion object {
        private const val TAG = "CommentNotifications"

        fun likeNotification(context: Context, data: JSONObject) {
            val notifier = data.getJSONObject("created_by").getString("username")
            val notifierImage = data.getJSONObject("created_by").getString("image")
            val commentId = data.getJSONObject("comment").getString("id")
            val commentContent = data.getJSONObject("comment").getString("content")

            val notificationId = ("${Constants.COMMENT_LIKE_NOTIFICATION_ID}$commentId").toInt()
            val notificationTitle = "$notifier liked your comment"
            //TODO find comment is of post or poll
            val notificationContent =
                commentContent.ifEmpty { "Tap to view post" }
            val notificationUserImage = FCMConfig.getBitmapFromUrl(notifierImage)

            val intent = Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(
                    context,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )

            val builder =
                NotificationCompat.Builder(context, Constants.COMMENT_NOTIFICATION_CHANNEL)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(notificationTitle)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentText(notificationContent)
                    .setLargeIcon(notificationUserImage)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()

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
            val commendId = data.getJSONObject("comment").getString("id")
            val commentContent = data.getJSONObject("comment").getString("content")
            val commentReply = data.getJSONObject("comment").getString("comment")

            val notificationId = ("${Constants.COMMENT_REPLY_NOTIFICATION_ID}$commendId").toInt()
            val notificationTitle = "$notifier replied on your comment"

            //TODO find comment is of post or poll
            val notificationContent =
                commentContent.ifEmpty { "Tap to view post" }

            val notificationUserImage = FCMConfig.getBitmapFromUrl(notifierImage)

            val intent = Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(
                    context,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )

            val builder =
                NotificationCompat.Builder(context, Constants.COMMENT_NOTIFICATION_CHANNEL)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(notificationTitle)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentText(notificationContent)
                    .setLargeIcon(notificationUserImage)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText("$notificationContent \n\n$commentReply")
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