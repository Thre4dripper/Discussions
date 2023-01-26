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

class PostNotifications {
    companion object {
        private const val TAG = "PostNotifications"

        fun likeNotification(
            context: Context, title: String?, content: String?, userImage: String?, postId: String?
        ) {
            Log.d(TAG, "likeNotification: $title, $content")
            val intent = Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val builder = NotificationCompat.Builder(context, Constants.POST_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentText(content)
                .setLargeIcon(FCMConfig.getBitmapFromUrl(userImage))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            with(NotificationManagerCompat.from(context)) {
                try {
                    notify(postId!!.toInt(), builder)
                } catch (e: SecurityException) {
                    Log.d(TAG, "e: $e")
                }
            }
        }
    }
}