package com.example.discussions.api.apiCalls.notification

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.CommentNotificationModel
import com.example.discussions.models.NotificationModel
import com.example.discussions.models.PollNotificationModel
import com.example.discussions.models.PostNotificationModel
import org.json.JSONArray
import org.json.JSONObject

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

                val post = notificationObject.get("post")
                val poll = notificationObject.get("poll")
                val comment = notificationObject.get("comment")

                notificationsList.add(
                    NotificationModel(
                        notificationObject.getString("id"),
                        notificationObject.getString("type"),
                        notificationObject.getBoolean("is_read"),
                        username,
                        userImage,
                        notificationObject.getString("created_at"),
                        if (post is JSONObject) parsePostNotificationJson(post) else null,
                        if (poll is JSONObject) parsePollNotificationJson(poll) else null,
                        if (comment is JSONObject) parseCommentNotificationJson(comment) else null,
                    )
                )
            }

            return notificationsList
        }

        private fun parsePostNotificationJson(post: JSONObject): PostNotificationModel {
            return PostNotificationModel(
                post.getString("id"),
                post.getString("title"),
                post.getString("content"),
                post.getJSONObject("created_by").getString("username"),
                post.getJSONObject("created_by").getString("image"),
                post.getString("created_at"),
                post.getString("post_image"),
                if (post.has("comment")) post.getString("comment") else null,
            )
        }

        private fun parsePollNotificationJson(poll: JSONObject): PollNotificationModel {
            return PollNotificationModel(
                poll.getString("id"),
                poll.getString("title"),
                poll.getString("content"),
                poll.getJSONObject("created_by").getString("username"),
                poll.getJSONObject("created_by").getString("image"),
                poll.getString("created_at"),
                if (poll.has("comment")) poll.getString("comment") else null,
            )
        }

        private fun parseCommentNotificationJson(comment: JSONObject): CommentNotificationModel {
            return CommentNotificationModel(
                comment.getString("id"),
                comment.getString("content"),
                comment.getJSONObject("created_by").getString("username"),
                comment.getJSONObject("created_by").getString("image"),
                comment.getString("created_at"),
                if (comment.has("comment")) comment.getString("comment") else null
            )
        }
    }
}