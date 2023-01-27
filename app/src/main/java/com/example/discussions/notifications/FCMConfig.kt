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

        val category = if (data.has("post"))
            Constants.NOTIFICATION_CATEGORY_POST
        else if (data.has("poll"))
            Constants.NOTIFICATION_CATEGORY_POLL
        else if (data.has("comment"))
            Constants.NOTIFICATION_CATEGORY_COMMENT
        else Constants.NOTIFICATION_CATEGORY_INVALID

        notifyByCategory(category, data)

        Log.d(TAG, "onMessageReceived: $data")
    }

    private fun notifyByCategory(category: String, data: JSONObject) {
        when (category) {
            Constants.NOTIFICATION_CATEGORY_POST -> {
                PostNotifications.likeNotification(this, data)
                //comment
            }
            Constants.NOTIFICATION_CATEGORY_POLL -> {
                //like
                //comment
                //vote
            }
            Constants.NOTIFICATION_CATEGORY_COMMENT -> {
                //like
                //comment
            }
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
        fun getBitmapFromUrl(imageUrl: String): Bitmap? {
            return try {
                val url = URL(imageUrl)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.inputStream
                return BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}