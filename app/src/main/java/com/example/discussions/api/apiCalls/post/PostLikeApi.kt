package com.example.discussions.api.apiCalls.post

import android.content.Context
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import org.json.JSONObject

class PostLikeApi {
    companion object {
        fun likePost(
            context: Context,
            postId: String,
            token: String,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.POST_LIKE}"

            val body = "{\n" +
                    "    \"post_id\": $postId\n" +
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