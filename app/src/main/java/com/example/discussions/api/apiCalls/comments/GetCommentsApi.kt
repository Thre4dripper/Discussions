package com.example.discussions.api.apiCalls.comments

import android.content.Context
import android.util.Log
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import org.json.JSONObject

class GetCommentsApi {
    companion object {
        private const val TAG = "GetCommentsApi"

        fun getComments(
            context: Context,
            token: String,
            postId: Int?,
            pollId: Int?,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.COMMENTS_GET_COMMENTS}"

            val body = JSONObject()
            if (postId != null) {
                body.put("post_id", postId)
            } else if (pollId != null) {
                body.put("poll_id", pollId)
            }

            val request = object : CustomRequest(Method.POST, url, body, { response ->
                callback.onSuccess(response.toString())
                Log.d(TAG, "getComments: $response")
            }, { err ->
                callback.onError(err.toString())
                Log.d(TAG, "getComments: $err")
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