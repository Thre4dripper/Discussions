package com.example.discussions

import android.app.Application

class MyApplication : Application() {
    companion object {
        lateinit var instance: MyApplication
        lateinit var username: String
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Cloudinary.setupCloudinary(this)
    }
}