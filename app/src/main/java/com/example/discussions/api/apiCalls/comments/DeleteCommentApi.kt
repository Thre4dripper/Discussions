package com.example.discussions.api.apiCalls.comments

import android.content.Context
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.CommentModel
import org.json.JSONObject

class DeleteCommentApi {
    companion object {
        fun deleteComment(
            context: Context,
            token: String,
            commentId: String?,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.COMMENTS_DELETE_COMMENT}"

            val body = JSONObject()
            body.put("comment_id", commentId)

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
    }
}