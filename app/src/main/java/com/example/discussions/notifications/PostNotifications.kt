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
import com.example.discussions.api.ResponseCallback
import com.example.discussions.repositories.PostRepository
import com.example.discussions.ui.home.HomeActivity

class PostNotifications {
    companion object {
        private const val TAG = "PostNotifications"
        val post = PostRepository.singlePost

        fun likeNotification(
            context: Context, title: String, userImage: String, postId: String
        ) {

            var notifierImage: Bitmap? = null
            var postImage: Bitmap? = null

            //for checking if both requests are completed
            var requestCount = 0

            //get user image asynchronously
            FCMConfig.getBitmapFromUrl(context, userImage) { bitmap ->
                notifierImage = bitmap
                requestCount++
                requestNotification(
                    context,
                    requestCount,
                    title,
                    notifierImage,
                    if (post.value != null) post.value!!.title + " " + post.value!!.content else "Tap to view post",
                    postId,
                    postImage
                )
            }

            //get post info asynchronously
            PostRepository.getPostByID(context, postId, object : ResponseCallback {
                override fun onSuccess(response: String) {
                    //after getting post info get post image asynchronously
                    FCMConfig.getBitmapFromUrl(context, post.value!!.postImage) { bitmap ->
                        postImage = bitmap
                            requestCount++

                            //fire notification with post content
                            requestNotification(
                                context,
                                requestCount,
                                title,
                                notifierImage,
                                post.value!!.title + " " + post.value!!.content,
                                postId,
                                postImage
                            )
                    }
                }

                //post info fetch failed
                override fun onError(response: String) {
                    requestCount++
                    //fire notification without post content with default content
                    requestNotification(
                        context,
                        requestCount,
                        title,
                        notifierImage,
                        "Tap to view post",
                        postId,
                        null
                    )
                }
            })
        }

        private fun requestNotification(
            context: Context,
            requestCount: Int,
            notificationTitle: String,
            notifierImage: Bitmap?,
            notificationContent: String,
            postId: String,
            postImage: Bitmap?
        ) {
            if (requestCount == 2) {
                fireNotification(
                    context,
                    notificationTitle,
                    notificationContent,
                    notifierImage,
                    postId,
                    postImage
                )
            }
        }

        private fun fireNotification(
            context: Context,
            title: String?,
            content: String?,
            userImage: Bitmap?,
            postId: String?,
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
                    notify(postId!!.toInt(), builder)
                } catch (e: SecurityException) {
                    Log.d(TAG, "e: $e")
                }
            }
        }
    }
}