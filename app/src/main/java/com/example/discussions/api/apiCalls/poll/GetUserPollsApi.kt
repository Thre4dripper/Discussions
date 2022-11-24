package com.example.discussions.api.apiCalls.poll

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.PollModel
import com.example.discussions.models.PollOptionModel
import com.example.discussions.models.PollVotedByModel
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

        fun parseUserPollsJson(json: String): MutableList<PollModel> {
            val rootObject = JSONArray(json)
            val pollsList = mutableListOf<PollModel>()

            for (i in 0 until rootObject.length()) {
                val pollObject = rootObject.getJSONObject(i)
                val createdByObject = pollObject.getJSONObject("created_by")
                var username = createdByObject.getString("username")
                val userImage = createdByObject.getString("image")
                username = "@$username"

                val pollOptionsList = mutableListOf<PollOptionModel>()
                val pollOptionsArray = pollObject.getJSONArray("poll_option")

                //this loop is used to parse poll options
                for (j in 0 until pollOptionsArray.length()) {
                    val pollOptionObject = pollOptionsArray.getJSONObject(j)

                    val votedByList = mutableListOf<PollVotedByModel>()
                    val votedByArray = pollOptionObject.getJSONArray("voted_by")

                    //this loop is used to parse voted by list
                    for (k in 0 until votedByArray.length()) {
                        val votedByObject = votedByArray.getJSONObject(k)

                        //filling voted by list
                        votedByList.add(
                            PollVotedByModel(
                                votedByObject.getString("id"),
                                votedByObject.getString("username"),
                                votedByObject.getString("image")
                            )
                        )
                    }

                    //filling poll options list
                    pollOptionsList.add(
                        PollOptionModel(
                            pollOptionObject.getString("id"),
                            pollOptionObject.getString("content"),
                            "",
                            pollOptionObject.getInt("votes"),
                            votedByList
                        )
                    )
                }

                //filling polls list
                pollsList.add(
                    PollModel(
                        pollObject.getString("id"),
                        pollObject.getString("title"),
                        pollObject.getString("content"),
                        pollObject.getInt("total_votes"),
                        pollObject.getBoolean("private"),
                        pollObject.getBoolean("is_voted"),
                        username,
                        userImage,
                        pollObject.getString("created_at"),
                        pollObject.getInt("like_count"),
                        pollObject.getInt("comment_count"),
                        pollOptionsList
                    )
                )
            }

            return pollsList
        }
    }
}