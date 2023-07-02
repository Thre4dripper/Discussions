package com.example.discussions.api.apiCalls.poll

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

class GetUserPollsApi {
    companion object {
        private const val TAG = "GetUserPollsApi"
        var queue: RequestQueue? = null
        fun getUserPollsJson(
            context: Context,
            token: String,
            page: Int,
            callback: ResponseCallback
        ) {
            queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.POLL_GET_USER_POLLS}" +
                    "?limit=${Constants.POLLS_PAGING_SIZE}" +
                    "&offset=${(page - 1) * Constants.POLLS_PAGING_SIZE}"

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

        fun parseUserPollsJson(json: String): MutableList<DiscussionModel> {
            val rootObject = JSONObject(json)
            val pollsList = mutableListOf<DiscussionModel>()
            val resultsArray = rootObject.getJSONArray("results")

            val next = rootObject.getString("next")
            val previous = rootObject.getString("previous")

            for (i in 0 until resultsArray.length()) {
                val pollObject = resultsArray.getJSONObject(i)
                val id = pollObject.getString("id")
                val poll =
                    GetPollByIdApi.parsePollByIdJson(pollObject.toString())
                val type = DiscussionsRecyclerAdapter.DISCUSSION_TYPE_POLL

                pollsList.add(
                    DiscussionModel(
                        "poll_$id",
                        rootObject.getInt("count"),
                        if (next == "null") null else next,
                        if (previous == "null") null else previous,
                        null,
                        poll,
                        type
                    )
                )
            }
            return pollsList
        }
    }
}