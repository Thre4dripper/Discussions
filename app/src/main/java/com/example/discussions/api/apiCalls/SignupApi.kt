package com.example.discussions.api.apiCalls

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import org.json.JSONObject

class SignupApi {
    companion object {
        private const val TAG = "SignupApi"

        fun signupUser(
            context: Context,
            username: String,
            email: String,
            password: String,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.REGISTER}"

            val body = "{\n" +
                    "    \"username\": ${username},\n" +
                    "    \"email\": ${email},\n" +
                    "    \"password\": ${password}\n" +
                    "}"

            val request =
                JsonObjectRequest(Request.Method.POST, url, JSONObject(body), { response ->
                    callback.onSuccess(response.toString())
                }, { err ->
                    //getting error data , if any
                    val networkResponse = err.networkResponse
                    if (networkResponse != null) {
                        //converting error data to string
                        val errorResponse = String(networkResponse.data, Charsets.UTF_8)
                        callback.onError(errorResponse)
                    } else {
                        callback.onError(err.toString())
                    }
                })

            queue.add(request)
        }
    }
}