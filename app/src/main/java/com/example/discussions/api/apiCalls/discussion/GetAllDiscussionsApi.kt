package com.example.discussions.api.apiCalls.discussion

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.adapters.DiscussionsRecyclerAdapter
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import com.example.discussions.api.apiCalls.poll.GetPollByIdApi
import com.example.discussions.api.apiCalls.post.GetPostByIdApi
import com.example.discussions.models.DiscussionModel
import com.example.discussions.models.PollModel
import com.example.discussions.models.PostModel
import org.json.JSONObject

class GetAllDiscussionsApi {
    companion object {
        fun getAllDiscussionsJson(
            context: Context,
            token: String,
            page: Int,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.DISCUSSIONS_GET_ALL}" +
                    "?limit=${ApiRoutes.DISCUSSIONS_LIMIT}" +
                    "&offset=${(page - 1) * ApiRoutes.DISCUSSIONS_LIMIT}"

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

        fun parseAllDiscussionsJson(json: String): MutableList<DiscussionModel> {
            val rootObject = JSONObject(json)
            val discussionsList = mutableListOf<DiscussionModel>()
            val resultsArray = rootObject.getJSONArray("results")

            val next = rootObject.getString("next")
            val previous = rootObject.getString("previous")

            for (i in 0 until resultsArray.length()) {
                val discussionObject = resultsArray.getJSONObject(i)

                val id = discussionObject.getString("id")
                val discussionType = discussionObject.getString("type")

                var post: PostModel? = null
                var poll: PollModel? = null
                var type: Int
                if (discussionType == "post") {
                    post =
                        GetPostByIdApi.parsePostByIdJson(resultsArray.getJSONObject(i).toString())
                    type = DiscussionsRecyclerAdapter.DISCUSSION_TYPE_POST
                } else {
                    poll =
                        GetPollByIdApi.parsePollByIdJson(resultsArray.getJSONObject(i).toString())
                    type = DiscussionsRecyclerAdapter.DISCUSSION_TYPE_POLL
                }
                discussionsList.add(
                    DiscussionModel(
                        if (discussionType == "post") "post_$id" else "poll_$id",
                        rootObject.getInt("count"),
                        if (next == "null") null else next,
                        if (previous == "null") null else previous,
                        post,
                        poll,
                        type
                    )
                )
            }
            return discussionsList
        }
    }
}