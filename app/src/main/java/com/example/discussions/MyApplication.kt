package com.example.discussions

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApplication : Application() {
    companion object {
        lateinit var instance: MyApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        setupCloudinary()
    }


    private fun setupCloudinary() {
        val config = HashMap<String, String>()
        config["cloud_name"] = getString(R.string.cloud_name)
        config["api_key"] = getString(R.string.api_key)
        config["api_secret"] = getString(R.string.api_secret)
        config["secure"] = "true"

        MediaManager.init(this, config)
    }
}