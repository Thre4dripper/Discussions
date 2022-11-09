package com.example.discussions.api.apiCalls.user

import android.content.Context
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.Constants
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import org.json.JSONObject

class NameImageApi {
    companion object {
        fun getUsernameAndImageJson(context: Context, token: String, callback: ResponseCallback) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.USER_USERNAME_IMAGE}"

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

            queue.add(request)
        }

        fun parseUsernameAndImageJson(json: String): Map<String, String> {
            val rootObject = JSONObject(json)

            //TODO simplify this when api is ready
            val username = if (rootObject.has("username")) rootObject.getString("username") else ""
            val profileImage = rootObject.getString("user_image")

            return mapOf(
                Constants.PROFILE_IMAGE to profileImage,
                Constants.USERNAME to username
            )
        }
    }
}