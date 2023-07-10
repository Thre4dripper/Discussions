package com.example.discussions

import android.app.Application
import com.example.discussions.store.UserStore

class MyApplication : Application() {
    private val TAG = "MyApplication"
    companion object {
        lateinit var instance: MyApplication
        lateinit var username: String
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        //username always be initialized very first
        username = UserStore.getUserName(this)!!
        Cloudinary.setupCloudinary(this)
    }
}