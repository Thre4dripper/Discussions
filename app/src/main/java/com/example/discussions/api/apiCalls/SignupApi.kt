package com.example.discussions.api.apiCalls

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import com.example.discussions.viewModels.LoginViewModel
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

            val body =
                "{\n" + "    \"username\": ${username},\n" + "    \"email\": ${email},\n" + "    \"password\": ${password}\n" + "}"

            val request =
                JsonObjectRequest(Request.Method.POST, url, JSONObject(body), { response ->
                    callback.onSuccess(response.toString())
                }, { err ->
                    //getting error data , if any
                    val data = err.networkResponse.data
                    if (data != null) {
                        //converting error data to string
                        val errorResponse = String(data, Charsets.UTF_8)
                        val error = parseErrorResponse(errorResponse)
                        callback.onError(error)
                    } else {
                        callback.onError(err.toString())
                    }
                })

            queue.add(request)
        }

        /**
         * Method for parsing error response and returning error message
         */
        private fun parseErrorResponse(response: String): String {
            val rootObject = JSONObject(response)
            val isUsernameExist = rootObject.has("username")
            val isEmailExist = rootObject.has("email")

            return if (isUsernameExist && isEmailExist) {
                LoginViewModel.API_ERROR_USERNAME_EMAIL
            } else if (isUsernameExist) {
                LoginViewModel.API_ERROR_USERNAME
            } else if (isEmailExist) {
                LoginViewModel.API_ERROR_EMAIL
            } else {
                LoginViewModel.API_ERROR
            }
        }
    }
}