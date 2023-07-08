package com.example.discussions.api.apiCalls.comments

import android.content.Context
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.CommentModel
import org.json.JSONArray
import org.json.JSONObject

class GetCommentsApi {
    companion object {
        private const val TAG = "GetCommentsApi"

        fun getCommentsJson(
            context: Context,
            token: String,
            postId: String?,
            pollId: String?,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.COMMENTS_GET_COMMENTS}"

            val body = JSONObject()
            if (postId != null) {
                body.put("post_id", postId)
            } else if (pollId != null) {
                body.put("poll_id", pollId)
            }

            val request = object : CustomRequest(Method.POST, url, body, { response ->
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

        fun parseCommentsJson(response: String): MutableList<CommentModel> {
            val rootObject = JSONArray(response)
            val commentsList = mutableListOf<CommentModel>()

            for (i in 0 until rootObject.length()) {
                val commentObject = rootObject.getJSONObject(i)
                val createdByObject = commentObject.getJSONObject("created_by")
                val username = createdByObject.getString("username")
                val userImage = createdByObject.getString("image")

                val commentId = commentObject.getString("id")
                val parentCommentId = commentObject.getString("parent_id")
                val comment = commentObject.getString("content")
                val createdAt = commentObject.getString("created_at")
                val isLiked = commentObject.getBoolean("is_liked")
                val likes = commentObject.getInt("like_count")

                val repliesList =
                    parseCommentsJson(commentObject.getJSONArray("replies").toString())

                commentsList.add(
                    CommentModel(
                        commentId,
                        if (parentCommentId == "null") null else parentCommentId,
                        comment,
                        username,
                        userImage,
                        createdAt,
                        isLiked,
                        likes,
                        repliesList
                    )
                )
            }

            return commentsList
        }
    }
}