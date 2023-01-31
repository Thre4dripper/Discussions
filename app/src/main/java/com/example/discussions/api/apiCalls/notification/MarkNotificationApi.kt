package com.example.discussions.api.apiCalls.notification

import android.content.Context
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import org.json.JSONObject

class MarkNotificationApi {
    companion object {
        fun readNotification(
            context: Context,
            token: String,
            notificationId: String,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.NOTIFICATION_MARK_AS_READ}"

            val body = "{\n" +
                    "    \"notification_id\": $notificationId\n" +
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