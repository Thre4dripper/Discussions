package com.example.discussions.api.apiCalls.post

import android.content.Context
import android.util.Log
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback

class DeletePostApi {
    companion object {
        private const val TAG = "DeletePostApi"

        fun deletePost(
            context: Context,
            postId: String,
            token: String,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.POST_DELETE_POST}$postId"

            val request = object : JsonObjectRequest(Method.DELETE, url, null, { response ->
                callback.onSuccess(response.toString())
            }, { error ->
                Log.d(TAG, "deletePost: $error")
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