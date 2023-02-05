package com.example.discussions.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.ui.PollDetailsActivity
import org.json.JSONObject

class PollNotifications {
    companion object {
        private const val TAG = "PollNotifications"

        fun likeNotification(context: Context, data: JSONObject) {
            val notifier = data.getJSONObject("created_by").getString("username")
            val notifierImage = data.getJSONObject("created_by").getString("image")
            val pollId = data.getJSONObject("poll").getString("id")
            val pollTitle = data.getJSONObject("poll").getString("title")
            val pollContent = data.getJSONObject("poll").getString("content")

            val notificationId = ("${Constants.POLL_LIKE_NOTIFICATION_ID}$pollId").toInt()
            val notificationTitle = "$notifier liked your poll"
            val notificationContent =
                if (pollTitle.isEmpty() && pollContent.isEmpty()) "Tap to view poll" else "$pollTitle $pollContent"
            val notificationUserImage = FCMConfig.getBitmapFromUrl(notifierImage)

            val intent = Intent(context, PollDetailsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            intent.putExtra(Constants.POLL_ID, pollId)
            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(
                    context,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )

            val builder = NotificationCompat.Builder(context, Constants.POLL_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationTitle)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentText(notificationContent)
                .setLargeIcon(notificationUserImage)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true).build()

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
            val pollId = data.getJSONObject("poll").getString("id")
            val pollTitle = data.getJSONObject("poll").getString("title")
            val pollContent = data.getJSONObject("poll").getString("content")
            val pollComment = data.getJSONObject("poll").getString("comment")

            val notificationId = ("${Constants.POLL_COMMENT_NOTIFICATION_ID}$pollId").toInt()
            val notificationTitle = "$notifier commented on your poll"
            val notificationContent =
                if (pollTitle.isEmpty() && pollContent.isEmpty()) "Tap to view poll"
                else "$pollTitle $pollContent"

            val notificationUserImage = FCMConfig.getBitmapFromUrl(notifierImage)

            val intent = Intent(context, PollDetailsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            intent.putExtra(Constants.POLL_ID, pollId)
            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(
                    context,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )

            val builder = NotificationCompat.Builder(context, Constants.POLL_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationTitle)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentText(notificationContent)
                .setLargeIcon(notificationUserImage)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("$notificationContent \n\n$pollComment")
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