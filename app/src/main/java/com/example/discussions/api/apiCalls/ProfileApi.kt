package com.example.discussions.api.apiCalls

import android.content.Context
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import org.json.JSONObject

class ProfileApi {
    companion object {
        private const val TAG = "ProfileApi"

        fun getProfileJson(
            context: Context, token: String, callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.PROFILE}"

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

        fun parseProfileJson(json: String): Map<String, String> {
            val rootObject = JSONObject(json)
            val username = rootObject.getString("username")
            val firstName = rootObject.getString("first_name")
            val lastName = rootObject.getString("last_name")
            val gender = if (rootObject.has("gender")) rootObject.getString("gender") else "M"
            val email = rootObject.getString("email")
            val mobileNo = rootObject.getString("mobile_no")
            val dob = rootObject.getString("dob")
            val address = rootObject.getString("address")

            return mapOf(
                "username" to username,
                "firstName" to firstName,
                "lastName" to lastName,
                "gender" to gender,
                "email" to email,
                "mobileNo" to mobileNo,
                "dob" to if (dob == "null") "" else dob,
                "address" to address,
            )
        }

        fun updateProfile(
            context: Context,
            token: String,
            imageUrl: String,
            username: String,
            firstName: String,
            lastName: String,
            gender: String,
            email: String,
            mobileNo: String,
            dob: String,
            address: String,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.UPDATE_PROFILE}"

            val body = "{\n" +
                    "    \"image\": \"$imageUrl\",\n" +
                    "    \"username\": \"$username\",\n" +
                    "    \"first_name\": \"$firstName\",\n" +
                    "    \"last_name\": \"$lastName\",\n" +
                    "    \"gender\": \"$gender\",\n" +
                    "    \"email\": \"$email\",\n" +
                    "    \"mobile_no\": \"$mobileNo\",\n" +
                    "    \"dob\": \"$dob\",\n" +
                    "    \"address\": \"$address\"\n" +
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