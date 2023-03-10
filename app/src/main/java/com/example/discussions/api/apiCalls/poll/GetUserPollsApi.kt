package com.example.discussions.api.apiCalls.poll

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.adapters.DiscussionsRecyclerAdapter
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.DiscussionModel
import org.json.JSONArray

class GetUserPollsApi {
    companion object {

        fun getUserPollsJson(
            context: Context,
            token: String,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.POLL_GET_USER_POSTS}"

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

        fun parseUserPollsJson(json: String): MutableList<DiscussionModel> {
            val rootObject = JSONArray(json)
            val pollsList = mutableListOf<DiscussionModel>()

            for (i in 0 until rootObject.length()) {
                val pollObject = rootObject.getJSONObject(i)

                val poll = GetPollByIdApi.parsePollByIdJson(pollObject.toString())
                val type = DiscussionsRecyclerAdapter.DISCUSSION_TYPE_POLL


                //filling polls list
                pollsList.add(
                    DiscussionModel(
                        pollObject.getString("id"),
                        0,
                        null,
                        null,
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