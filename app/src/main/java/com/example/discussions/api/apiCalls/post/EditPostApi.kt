package com.example.discussions.api.apiCalls.post

import android.content.Context
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import org.json.JSONObject

class EditPostApi {
    companion object {

        fun updatePost(
            context: Context,
            token: String,
            postId: String,
            postTitle: String,
            postContent: String,
            postImage: String?,
            allowComments: Boolean,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.POST_UPDATE_POST}$postId/"

            val body = "{\n" +
                    "    \"title\": \"$postTitle\",\n" +
                    "    \"content\": \"$postContent\",\n" +
                    "    \"allow_comments\": $allowComments,\n" +
                    "    \"post_image\": \"$postImage\"\n" +
                    "}"

            val request =
                object : JsonObjectRequest(Method.PUT, url, JSONObject(body), { response ->
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