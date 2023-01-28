package com.example.discussions.api.apiCalls.notification

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.NotificationModel
import org.json.JSONArray

class GetAllNotificationsApi {
    companion object {
        fun getAllNotificationsJson(
            context: Context,
            token: String,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.NOTIFICATIONS_GET_ALL}"

            val request = object : JsonArrayRequest(Method.GET, url, null, { response ->
                callback.onSuccess(response.toString())
            }, { error ->
                callback.onError(error.toString())
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer $token"
                    return headers
                }
            }

            request.retryPolicy = DefaultRetryPolicy(
                15000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )

            queue.add(request)
        }

        fun parseAllNotificationsJson(response: String): MutableList<NotificationModel> {
            val rootObject = JSONArray(response)
            val notificationsList = mutableListOf<NotificationModel>()

            for (i in 0 until rootObject.length()) {
                val notificationObject = rootObject.getJSONObject(i)
                val createdByObject = notificationObject.getJSONObject("created_by")
                var username = createdByObject.getString("username")
                val userImage = createdByObject.getString("image")
                username = "@$username"

                val postId = notificationObject.getString("post")
                val pollId = notificationObject.getString("poll")
                val commentId = notificationObject.getString("comment")

                notificationsList.add(
                    NotificationModel(
                        notificationObject.getString("id"),
                        notificationObject.getString("type"),
                        notificationObject.getBoolean("is_read"),
                        username,
                        userImage,
                        notificationObject.getString("created_at"),
                        if (postId == "null") null else postId,
                        if (pollId == "null") null else pollId,
                        if (commentId == "null") null else commentId
                    )
                )
            }

            return notificationsList
        }
    }
}