package com.example.discussions.api.apiCalls.notification

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.Constants
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.CommentNotificationModel
import com.example.discussions.models.NotificationModel
import com.example.discussions.models.PollNotificationModel
import com.example.discussions.models.PostNotificationModel
import org.json.JSONObject

class GetAllNotificationsApi {
    companion object {
        private const val TAG = "GetAllNotificationsApi"
        var queue: RequestQueue? = null
        fun getAllNotificationsJson(
            context: Context,
            token: String,
            page: Int,
            callback: ResponseCallback
        ) {
            queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.NOTIFICATIONS_GET_ALL}" +
                    "?limit=${Constants.NOTIFICATIONS_PAGING_SIZE}" +
                    "&offset=${(page - 1) * Constants.NOTIFICATIONS_PAGING_SIZE}"

            val request = object : JsonObjectRequest(Method.GET, url, null, { response ->
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

            request.tag = TAG
            queue!!.add(request)
        }

        fun cancelGetRequest() {
            queue?.cancelAll(TAG)
        }

        fun parseAllNotificationsJson(json: String): MutableList<NotificationModel> {
            val rootObject = JSONObject(json)
            val notificationsList = mutableListOf<NotificationModel>()
            val resultsArray = rootObject.getJSONArray("results")

            val next = rootObject.getString("next")
            val previous = rootObject.getString("previous")

            for (i in 0 until resultsArray.length()) {
                val notificationObject = resultsArray.getJSONObject(i)

                val createdByObject = notificationObject.getJSONObject("created_by")
                val username = createdByObject.getString("username")
                val userImage = createdByObject.getString("image")

                val post = notificationObject.get("post")
                val poll = notificationObject.get("poll")
                val comment = notificationObject.get("comment")

                val category = if (post is JSONObject) {
                    Constants.NOTIFICATION_CATEGORY_POST
                } else if (poll is JSONObject) {
                    Constants.NOTIFICATION_CATEGORY_POLL
                } else if (comment is JSONObject) {
                    Constants.NOTIFICATION_CATEGORY_COMMENT
                } else {
                    Constants.NOTIFICATION_CATEGORY_INVALID
                }


                notificationsList.add(
                    NotificationModel(
                        notificationObject.getString("id"),
                        rootObject.getInt("count"),
                        if (next == "null") null else next,
                        if (previous == "null") null else previous,
                        notificationObject.getString("type"),
                        category,
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