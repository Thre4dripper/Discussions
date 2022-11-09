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
            context: Context, token: String, callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.USER_PROFILE}"

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

        fun parseProfileJson(json: String): ProfileDataModel {
            val rootObject = JSONObject(json)
            val userId = rootObject.getInt("user")
            val profileImage = rootObject.getString("user_image")
            var username = rootObject.getString("username")
            val firstName = rootObject.getString("first_name")
            val lastName = rootObject.getString("last_name")
            val postsCount = rootObject.getInt("user_post_count")
            val pollsCount = rootObject.getInt("user_poll_count")

            username = "@$username"
            return ProfileDataModel(
                userId.toString(),
                profileImage,
                username,
                firstName,
                lastName,
                postsCount.toString(),
                pollsCount.toString()
            )
        }
    }
}