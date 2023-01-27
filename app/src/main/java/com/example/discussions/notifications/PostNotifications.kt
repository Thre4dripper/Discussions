package com.example.discussions.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.ui.home.HomeActivity
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

            val notificationContent =
                if (postTitle.isEmpty() && postContent.isEmpty()) "Tap to view post" else "$postTitle $postContent"

            notify(
                context,
                notificationId = ("${Constants.POST_LIKE_NOTIFICATION_ID}$postId").toInt(),
                title = "$notifier liked your post",
                content = notificationContent,
                userImage = FCMConfig.getBitmapFromUrl(notifierImage),
                postImage = FCMConfig.getBitmapFromUrl(postImage)
            )
        }

        private fun notify(
            context: Context,
            notificationId: Int,
            title: String?,
            content: String?,
            userImage: Bitmap?,
            postImage: Bitmap?
        ) {
            val intent = Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val builder = NotificationCompat.Builder(context, Constants.POST_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title)
                .setPriority(NotificationCompat.PRIORITY_MAX).setContentText(content)
                .setLargeIcon(userImage).setStyle(
                    NotificationCompat.BigPictureStyle().bigPicture(postImage)
                ).setContentIntent(pendingIntent).setAutoCancel(true).build()

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