package com.example.discussions.viewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.discussions.api.ResponseCallback
import com.example.discussions.repositories.AuthRepository
import com.example.discussions.store.LoginStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val API_SUCCESS = "success"
    }

    private val TAG = "LoginViewModel"
    var username: String = ""
    var email: String = ""
    var password: String = ""
    var rememberMe: Boolean = false

    var loginStore: LoginStore = LoginStore(application)
    var isAuthenticated = MutableLiveData<String?>(null)
    var isRegistered = MutableLiveData<String?>(null)

    fun checkLoginStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            val token = loginStore.getJWTToken()
            if (token != null) {
                isAuthenticated.postValue(API_SUCCESS)
            }
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
                isAuthenticated.value = API_SUCCESS
                //also logged session will be saved
                if (rememberMe) {
                    viewModelScope.launch(Dispatchers.IO) {
                        loginStore.saveJWTToken(response)
                    }
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
                isRegistered.value = API_SUCCESS
            }

            override fun onError(response: String) {
                isRegistered.value = response
            }
        })
    }
}