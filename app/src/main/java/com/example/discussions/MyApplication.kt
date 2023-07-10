package com.example.discussions

import android.app.Application
import com.example.discussions.store.UserStore

class MyApplication : Application() {
    companion object {
        lateinit var instance: MyApplication
        lateinit var username: String

        fun isUsernameInitialized(): Boolean {
            return ::username.isInitialized
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        username = UserStore.getUserName(this)!!
        Cloudinary.setupCloudinary(this)
    }
}