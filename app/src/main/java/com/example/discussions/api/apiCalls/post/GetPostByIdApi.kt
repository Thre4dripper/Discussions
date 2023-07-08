package com.example.discussions.api.apiCalls.post

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.PostModel
import org.json.JSONObject

class GetPostByIdApi {
    companion object {
        private const val TAG = "GetPostByIdApi"

        fun getPostByIdJson(
            context: Context,
            token: String,
            postId: String,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.POST_GET_POST_BY_ID}$postId/"

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

            queue.add(request)
        }

        fun parsePostByIdJson(json: String): PostModel {
            val rootObject = JSONObject(json)

            val createdByObject = rootObject.getJSONObject("created_by")
            val username = createdByObject.getString("username")
            val userImage = createdByObject.getString("image")

            return PostModel(
                rootObject.getString("id"),
                rootObject.getString("title"),
                rootObject.getString("content"),
                username,
                userImage,
                rootObject.getString("created_at"),
                rootObject.getString("post_image"),
                rootObject.getBoolean("is_liked"),
                rootObject.getInt("like_count"),
                rootObject.getBoolean("allow_comments"),
                rootObject.getInt("comment_count")
            )
        }
    }
}