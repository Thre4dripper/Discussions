package com.example.discussions.api.apiCalls.comments

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.Constants
import com.example.discussions.adapters.CommentsRecyclerAdapter
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.CommentModel
import org.json.JSONArray
import org.json.JSONObject

class GetCommentsApi {
    companion object {
        private const val TAG = "GetCommentsApi"
        var queue: RequestQueue? = null

        fun getCommentsJson(
            context: Context,
            token: String,
            page: Int,
            postId: String?,
            pollId: String?,
            callback: ResponseCallback
        ) {
            queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.COMMENTS_GET_COMMENTS}" +
                    "?limit=${Constants.COMMENTS_PAGING_SIZE}" +
                    "&offset=${(page - 1) * Constants.COMMENTS_PAGING_SIZE}"

            val body = JSONObject()
            if (postId != null) {
                body.put("post_id", postId)
            } else if (pollId != null) {
                body.put("poll_id", pollId)
            }

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

            request.tag = TAG
            queue!!.add(request)
        }

        fun cancelGetRequest() {
            queue?.cancelAll(TAG)
        }

        fun parseCommentsJson(json: String): MutableList<CommentModel> {
            val rootObject = JSONObject(json)
            val resultsArray = rootObject.getJSONArray("results")

            val count = rootObject.getInt("count")
            val next = rootObject.getString("next")
            val previous = rootObject.getString("previous")

            return recursiveParse(resultsArray, count, next, previous)
        }

        private fun recursiveParse(
            json: JSONArray,
            count: Int,
            next: String?,
            previous: String?
        ): MutableList<CommentModel> {
            val commentsList = mutableListOf<CommentModel>()
            for (i in 0 until json.length()) {
                val commentObject = json.getJSONObject(i)

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
                    recursiveParse(commentObject.getJSONArray("replies"), 0, null, null)

                commentsList.add(
                    CommentModel(
                        commentId,
                        count,
                        if (next == "null") null else next,
                        if (previous == "null") null else previous,
                        CommentsRecyclerAdapter.COMMENTS_TYPE_COMMENT,
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