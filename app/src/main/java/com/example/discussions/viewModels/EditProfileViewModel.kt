package com.example.discussions.viewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.discussions.api.ResponseCallback
import com.example.discussions.repositories.ProfileRepository
import com.example.discussions.store.LoginStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditProfileViewModel(application: Application) : AndroidViewModel(application) {
    var firstName: String = ""
    var lastName: String = ""
    var gender: String = ""
    var email: String = ""
    var mobileNo: String = ""
    var dob: String = ""
    var address: String = ""

    private var loginStore: LoginStore = LoginStore(application)

    private val _isProfileLoaded = MutableLiveData<Boolean>(null)

    val isProfileLoaded: LiveData<Boolean>
        get() = _isProfileLoaded

    fun getProfile(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = loginStore.getJWTToken()
            //JWT exists
            if (token != null) {
                ProfileRepository.getProfile(context, token, object : ResponseCallback {
                    override fun onSuccess(response: String) {
                        ProfileRepository.map["firstName"]?.let { firstName = it }
                        ProfileRepository.map["lastName"]?.let { lastName = it }
                        ProfileRepository.map["gender"]?.let { gender = it }
                        ProfileRepository.map["email"]?.let { email = it }
                        ProfileRepository.map["mobileNo"]?.let { mobileNo = it }
                        ProfileRepository.map["dob"]?.let { dob = it }
                        ProfileRepository.map["address"]?.let { address = it }
                        _isProfileLoaded.postValue(true)
                    }

                    override fun onError(response: String) {
                        _isProfileLoaded.postValue(false)
                    }
                })
            }
            //JWT does not exist
            else {
                _isProfileLoaded.postValue(false)
            }
        }
    }
}