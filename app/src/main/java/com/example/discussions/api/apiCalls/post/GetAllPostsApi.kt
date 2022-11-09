package com.example.discussions.api.apiCalls.post

import android.content.Context
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.PostModel
import org.json.JSONArray

class GetAllPostsApi {
    companion object {
        private const val TAG = "GetAllPostsApi"

        fun getAllPostsJson(
            context: Context,
            token: String,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.POST_GET_POSTS}"

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

            queue.add(request)
        }

        fun parseAllPostsJson(json: String): MutableList<PostModel> {
            val rootObject = JSONArray(json)
            val postsList = mutableListOf<PostModel>()
            for (i in 0 until rootObject.length()) {
                val postObject = rootObject.getJSONObject(i)
                postsList.add(
                    PostModel(
                        postObject.getString("id"),
                        postObject.getString("title"),
                        postObject.getString("content"),
                        postObject.getString("created_by"),
                        postObject.getString("created_at"),
                        postObject.getString("post_image"),
                        postObject.getInt("like_count"),
                        postObject.getInt("comment_count")
                    )
                )
            }

            return postsList
        }
    }
}