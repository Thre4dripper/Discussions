package com.example.discussions.api.apiCalls.auth

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import org.json.JSONObject

class LoginApi {
    companion object {
        private const val TAG = "LoginApi"

        fun loginUser(
            context: Context,
            username: String,
            password: String,
            deviceToken: String,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.LOGIN}"

            val body = "{\n" +
                    "    \"username\": \"$username\",\n" +
                    "    \"password\": \"$password\",\n" +
                    "    \"fcm_token\": \"$deviceToken\"\n" +
                    "}"

            val request =
                JsonObjectRequest(Request.Method.POST, url, JSONObject(body), { response ->
                    callback.onSuccess(response.toString())
                }, { err ->
                    callback.onError(err.toString())
                })

            queue.add(request)
        }
    }
}