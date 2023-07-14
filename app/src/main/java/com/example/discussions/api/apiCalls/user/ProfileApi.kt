package com.example.discussions.api.apiCalls.user

import android.content.Context
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.ProfileDataModel
import org.json.JSONObject

class ProfileApi {
    companion object {
        private const val TAG = "ProfileApi"

        fun getProfileJson(
            context: Context, token: String, username: String, callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.USER_PROFILE}"

            val body = JSONObject()
            body.put("username", username)

            val request = object : JsonObjectRequest(Method.POST, url, body, { response ->
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

        fun parseProfileJson(json: String): ProfileDataModel {
            val rootObject = JSONObject(json)
            val userId = rootObject.getString("user")
            val profileImage = rootObject.getString("user_image")
            val username = rootObject.getString("username")
            val firstName = rootObject.getString("first_name")
            val lastName = rootObject.getString("last_name")
            val postsCount = rootObject.getString("user_post_count")
            val pollsCount = rootObject.getString("user_poll_count")

            return ProfileDataModel(
                userId,
                profileImage,
                username,
                firstName,
                lastName,
                postsCount,
                pollsCount
            )
        }
    }
}