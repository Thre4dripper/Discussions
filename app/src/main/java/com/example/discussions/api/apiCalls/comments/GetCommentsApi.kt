package com.example.discussions.api.apiCalls.comments

import android.content.Context
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.CommentModel
import com.example.discussions.models.ReplyCommentModel
import org.json.JSONArray
import org.json.JSONObject

class GetCommentsApi {
    companion object {
        private const val TAG = "GetCommentsApi"

        fun getCommentsJson(
            context: Context,
            token: String,
            postId: Int?,
            pollId: Int?,
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
                var username = createdByObject.getString("username")
                val userImage = createdByObject.getString("image")
                username = "@$username"

                val commentId = commentObject.getString("id")
                val comment = commentObject.getString("content")
                val createdAt = commentObject.getString("created_at")

                val repliesArray = commentObject.getJSONArray("reply")
                val repliesList = mutableListOf<ReplyCommentModel>()

                // Parse replies
                for (j in 0 until repliesArray.length()) {
                    val replyObject = repliesArray.getJSONObject(j)
                    val replyCreatedByObject = replyObject.getJSONObject("created_by")
                    var replyUsername = replyCreatedByObject.getString("username")
                    val replyUserImage = replyCreatedByObject.getString("image")
                    replyUsername = "@$replyUsername"

                    val replyId = replyObject.getString("id")
                    val reply = replyObject.getString("content")
                    val replyCreatedAt = replyObject.getString("created_at")

                    //adding replies to list
                    val replyCommentModel = ReplyCommentModel(
                        commentId,
                        replyId,
                        reply,
                        replyUsername,
                        replyUserImage,
                        replyCreatedAt
                    )
                    repliesList.add(replyCommentModel)
                }

                //adding comment to list
                val commentModel = CommentModel(
                    commentId,
                    comment,
                    username,
                    userImage,
                    createdAt,
                    repliesList
                )
                commentsList.add(commentModel)
            }

            return commentsList
        }
    }
}