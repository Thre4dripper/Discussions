package com.example.discussions.api.apiCalls.post

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.PostModel
import org.json.JSONArray

class GetUserPostsApi {
    companion object {
        fun getUserPostsJson(
            context: Context,
            userId: String,
            token: String,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.POST_GET_USER_POSTS}$userId/"

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

        fun parseUserPostsJson(json: String): MutableList<PostModel> {
            val rootObject = JSONArray(json)
            val postsList = mutableListOf<PostModel>()
            for (i in 0 until rootObject.length()) {
                val postObject = rootObject.getJSONObject(i)
                val createdByObject = postObject.getJSONObject("created_by")
                var username = createdByObject.getString("username")
                val userImage = createdByObject.getString("image")
                username = "@$username"

                postsList.add(
                    PostModel(
                        postObject.getString("id"),
                        postObject.getString("title"),
                        postObject.getString("content"),
                        username,
                        userImage,
                        postObject.getString("created_at"),
                        postObject.getString("post_image"),
                        postObject.getBoolean("is_liked"),
                        postObject.getInt("like_count"),
                        postObject.getBoolean("allow_comments"),
                        postObject.getInt("comment_count")
                    )
                )
            }

            return postsList
        }
    }
}