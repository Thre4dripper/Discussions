package com.example.discussions.api.apiCalls.poll

import android.content.Context
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback

class DeletePollApi {
    companion object {
        private const val TAG = "DeletePollApi"

        fun deletePoll(
            context: Context,
            pollId: String,
            token: String,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.POLL_DELETE_POLL}$pollId/"

            val request = object : JsonObjectRequest(Method.POST, url, null, { response ->
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