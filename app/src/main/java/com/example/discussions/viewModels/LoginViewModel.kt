package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Constants
import com.example.discussions.MyApplication
import com.example.discussions.api.ResponseCallback
import com.example.discussions.repositories.AuthRepository
import com.example.discussions.store.LoginStore

class LoginViewModel : ViewModel() {
    private val TAG = "LoginViewModel"

    var username = MutableLiveData<String>()
    var email = MutableLiveData<String>()
    var password = MutableLiveData<String>()
    var rememberMe = MutableLiveData<Boolean>()

    var isAuthenticated = MutableLiveData<String?>(null)
    var isRegistered = MutableLiveData<String?>(null)

    fun checkLoginStatus(context: Context) {
        val loginStatus = LoginStore.getLoginStatus(context)
        val token = LoginStore.getJWTToken(context)
        if (loginStatus && token != null) {
            MyApplication.username = LoginStore.getUserName(context)!!
            isAuthenticated.postValue(Constants.API_SUCCESS)
        }
    }

    fun login(
        context: Context,
        username: String,
        password: String,
        rememberMe: Boolean,
    ) {
        AuthRepository.loginUser(context, username, password, object : ResponseCallback {
            override fun onSuccess(response: String) {
                isAuthenticated.value = Constants.API_SUCCESS
                //also logged session will be saved
                LoginStore.saveJWTToken(context, response)
                LoginStore.saveUserName(context, username)
                MyApplication.username = username
                if (rememberMe) {
                    LoginStore.saveLoginStatus(context, true)
                }
            }

            override fun onError(response: String) {
                isAuthenticated.value = response
            }
        })
    }


    fun signup(
        context: Context,
        username: String,
        email: String,
        password: String,
    ) {
        AuthRepository.signupUser(context, username, email, password, object : ResponseCallback {
            override fun onSuccess(response: String) {
                isRegistered.value = Constants.API_SUCCESS
            }

            override fun onError(response: String) {
                isRegistered.value = response
            }
        })
    }
}