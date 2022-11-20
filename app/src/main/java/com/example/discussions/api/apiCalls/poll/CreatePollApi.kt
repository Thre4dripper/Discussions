package com.example.discussions.api.apiCalls.poll

import android.content.Context
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.PollOptionModel
import org.json.JSONObject

class CreatePollApi {
    companion object {
        private const val TAG = "CreatePollApi"

        fun createPoll(
            context: Context,
            token: String,
            pollTitle: String,
            pollContent: String,
            pollOptions: List<PollOptionModel>,
            isPrivate: Boolean,
            allowComments: Boolean,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.POLL_CREATE}"

            var pollOptionsJson = ""
            pollOptions.forEachIndexed { index, pollOptionModel ->
                pollOptionsJson += "    \"poll_option${index + 1}\": \"${pollOptionModel.option}\",\n"
            }

            pollOptionsJson = pollOptionsJson.dropLast(2)

            val body = "{\n" +
                    "    \"title\": \"$pollTitle\",\n" +
                    "    \"content\": \"$pollContent\",\n" +
                    "    \"allow_comments\": $allowComments,\n" +
                    pollOptionsJson + "\n" +
                    "}"

            val request =
                object : JsonObjectRequest(Method.POST, url, JSONObject(body), { response ->
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
    }
}