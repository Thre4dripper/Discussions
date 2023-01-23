package com.example.discussions.api.apiCalls.comments

import android.content.Context
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import org.json.JSONObject

class UpdateCommentApi {
    companion object {
        fun updateComment(
            context: Context,
            token: String,
            commentId: String,
            content: String,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.COMMENTS_UPDATE_COMMENT}"

            val body = JSONObject()
            body.put("comment_id", commentId)
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
    }
}