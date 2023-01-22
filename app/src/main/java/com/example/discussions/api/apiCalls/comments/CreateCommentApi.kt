package com.example.discussions.api.apiCalls.comments

import android.content.Context
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.CommentModel
import org.json.JSONObject

class CreateCommentApi {
    companion object {
        fun createComment(
            context: Context,
            token: String,
            postId: String?,
            pollId: String?,
            commentId: String?,
            content: String,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.COMMENTS_CREATE_COMMENT}"

            val body = JSONObject()
            if (postId != null) {
                body.put("post_id", postId)
            } else if (pollId != null) {
                body.put("poll_id", pollId)
            } else if (commentId != null) {
                body.put("comment_id", commentId)
            }

            body.put("content", content)

            val request = object : JsonObjectRequest(Method.POST, url, body, { response ->
                callback.onSuccess(response.toString())
            }, { err ->
                callback.onError(err.toString())
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer $token"
                    return headers
                }
            }

            queue.add(request)
        }

        fun parseCreateCommentJson(response: String): CommentModel {
            val rootObject = JSONObject(response)
            val createdByObject = rootObject.getJSONObject("created_by")
            var username = createdByObject.getString("username")
            val userImage = createdByObject.getString("image")
            username = "@$username"

            val commentId = rootObject.getString("id")
            val comment = rootObject.getString("content")
            val createdAt = rootObject.getString("created_at")

            val repliesArray = rootObject.getJSONArray("reply")
            val repliesList = mutableListOf<CommentModel>()

            for (i in 0 until repliesArray.length()) {
                val replyObject = repliesArray.getJSONObject(i)
                val replyCreatedByObject = replyObject.getJSONObject("created_by")
                var replyUsername = replyCreatedByObject.getString("username")
                val replyUserImage = replyCreatedByObject.getString("image")
                replyUsername = "@$replyUsername"

                val replyId = replyObject.getString("id")
                val reply = replyObject.getString("content")
                val replyCreatedAt = replyObject.getString("created_at")

                //adding replies to list
                val replyCommentModel = CommentModel(
                    replyId,
                    commentId,
                    reply,
                    replyUsername,
                    replyUserImage,
                    replyCreatedAt,
                    mutableListOf()
                )

                repliesList.add(replyCommentModel)
            }

            return CommentModel(
                commentId, null, username, userImage, comment, createdAt, repliesList
            )
        }
    }
}