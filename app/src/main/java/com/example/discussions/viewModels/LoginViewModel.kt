package com.example.discussions.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    private val TAG = "LoginViewModel"
    var username: String = ""
    var email: String = ""
    var password: String = ""
    var rememberMe: Boolean = false

    fun login() {
        Log.d(TAG, "login: $username, $email, $password, $rememberMe")
    }
}