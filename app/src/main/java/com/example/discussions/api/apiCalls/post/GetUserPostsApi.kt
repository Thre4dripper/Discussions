package com.example.discussions.api.apiCalls.post

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.adapters.DiscussionsRecyclerAdapter
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.DiscussionModel

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

        fun parseUserPostsJson(json: String): MutableList<DiscussionModel> {
            val rootObject = JSONArray(json)
            val postsList = mutableListOf<DiscussionModel>()
            for (i in 0 until rootObject.length()) {
                val postObject = rootObject.getJSONObject(i)

                val post = GetPostByIdApi.parsePostByIdJson(postObject.toString())
                val type = DiscussionsRecyclerAdapter.DISCUSSION_TYPE_POST
                postsList.add(
                    DiscussionModel(
                        postObject.getString("id"),
                        0,
                        null,
                        null,
                        post,
                        null,
                        type
                    )
                )
            }

            return postsList
        }
    }
}