package com.example.discussions.viewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.discussions.api.ResponseCallback
import com.example.discussions.repositories.LoginRepository

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val API_SUCCESS = "success"
    }

    private val TAG = "LoginViewModel"
    var username: String = ""
    var email: String = ""
    var password: String = ""
    var rememberMe: Boolean = false

    var isAuthenticated = MutableLiveData("")

    fun login(
        context: Context,
        username: String,
        password: String,
        rememberMe: Boolean,
    ) {
        LoginRepository.loginUser(context, username, password, object : ResponseCallback {
            override fun onSuccess(response: String) {
                isAuthenticated.value = API_SUCCESS
            }

            override fun onError(response: String) {
                isAuthenticated.value = response
            }
        })
    }
}