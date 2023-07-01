package com.example.discussions.api.apiCalls.post

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.Constants
import com.example.discussions.adapters.DiscussionsRecyclerAdapter
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.DiscussionModel
import org.json.JSONObject

class GetUserPostsApi {
    companion object {
        private const val TAG = "GetUserPostsApi"
        var queue: RequestQueue? = null
        fun getUserPostsJson(
            context: Context,
            userId: String,
            token: String,
            page: Int,
            callback: ResponseCallback
        ) {
            queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.POST_GET_USER_POSTS}$userId" +
                    "?limit=${Constants.PROFILE_POSTS_PAGING_SIZE}" +
                    "&offset=${(page - 1) * Constants.PROFILE_POSTS_PAGING_SIZE}"

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

            request.tag = TAG
            queue!!.add(request)
        }

        fun cancelGetRequest() {
            queue?.cancelAll(TAG)
        }

        fun parseUserPostsJson(json: String): MutableList<DiscussionModel> {
            val rootObject = JSONObject(json)
            val postsList = mutableListOf<DiscussionModel>()
            val resultsArray = rootObject.getJSONArray("results")

            val next = rootObject.getString("next")
            val previous = rootObject.getString("previous")

            for (i in 0 until resultsArray.length()) {
                val postObject = resultsArray.getJSONObject(i)
                val id = postObject.getString("id")
                val post =
                    GetPostByIdApi.parsePostByIdJson(postObject.toString())
                val type = DiscussionsRecyclerAdapter.DISCUSSION_TYPE_POST

                postsList.add(
                    DiscussionModel(
                        "post_$id",
                        rootObject.getInt("count"),
                        if (next == "null") null else next,
                        if (previous == "null") null else previous,
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