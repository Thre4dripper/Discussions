package com.example.discussions.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import com.example.discussions.Constants
import com.example.discussions.store.UserStore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class FCMConfig : FirebaseMessagingService() {
    private val TAG = "FCMConfig"
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        UserStore.saveDeviceToken(this, token)
        Log.d(TAG, "onNewToken: $token")

        //create posts notification channel
        createNotificationChannel(
            Constants.POST_NOTIFICATION_CHANNEL,
            "Posts",
            "Posts notifications"
        )

        //create poll notification channel
        createNotificationChannel(
            Constants.POLL_NOTIFICATION_CHANNEL,
            "Polls",
            "Polls notifications"
        )

        //create comment notification channel
        createNotificationChannel(
            Constants.COMMENT_NOTIFICATION_CHANNEL,
            "Comments",
            "Comments notifications"
        )

    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = JSONObject(message.data.toString())
        val type = data.getString("type")
        val username = data.getJSONObject("created_by").getString("username")
        val image = data.getJSONObject("created_by").getString("image")

        val postId = if (data.has("post")) data.getString("post") else null
        val pollId = if (data.has("poll")) data.getString("poll") else null
        val commentId = if (data.has("comment")) data.getString("comment") else null

        if (postId != null) {
            val title = getTitle(type, username)
            val content = "Tap to view"
            PostNotifications.likeNotification(this, title, content, image, postId)
        }

        Log.d(TAG, "onMessageReceived: $data")
    }

    private fun getTitle(type: String, username: String): String {
        return when (type) {
            Constants.LIKE -> "$username liked your post"
            Constants.COMMENT -> "$username commented on your post"
            Constants.VOTE -> "$username voted on your poll"
            else -> "Unknown notification"
        }
    }

    private fun createNotificationChannel(
        channelId: String,
        channelName: String,
        channelDescription: String,
    ) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        fun getBitmapFromUrl(imageUrl: String?): Bitmap? {
            return try {
                val url = URL(imageUrl)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.inputStream
                BitmapFactory.decodeStream(input)
            } catch (e: IOException) {
                // Log exception
                e.printStackTrace()
                null
            }
        }
    }
}