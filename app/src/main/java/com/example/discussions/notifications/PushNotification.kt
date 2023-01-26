package com.example.discussions.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.discussions.R
import com.example.discussions.store.UserStore
import com.example.discussions.ui.home.HomeActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class PushNotification : FirebaseMessagingService() {
    private val TAG = "PushNotification"
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        UserStore.saveDeviceToken(this, token)
        Log.d(TAG, "onNewToken: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
        val content = message.notification?.body
        val imageUrl = message.data["url"]
        createNotificationChannel()

        val builder = createNotificationBuilder(title, content, imageUrl)

        with(NotificationManagerCompat.from(this)) {
            try {
                notify(1, builder.build())
            } catch (e: SecurityException) {
                Log.d(TAG, "e: $e")
            }
        }

        val body = "$title" + "$content" + "$imageUrl"

        Log.d(TAG, "onMessageReceived: $body")
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "channelName"
            val descriptionText = "channelDescription"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotificationBuilder(
        title: String?, content: String?, imageUrl: String?
    ): NotificationCompat.Builder {
        // Layouts for the custom notification
//        val notificationLayout = RemoteViews(packageName, R.layout.notification_collapsed)
//        val notificationLayoutExpanded = RemoteViews(packageName, R.layout.notification_expanded)

        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Apply the layouts to the notification builder
        return NotificationCompat
            .Builder(this, "CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_like)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(getBitmapFromUrl(imageUrl))
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).setContentIntent(pendingIntent)
    }

    private fun getBitmapFromUrl(imageUrl: String?): Bitmap? {
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