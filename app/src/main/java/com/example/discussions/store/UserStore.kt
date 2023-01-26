package com.example.discussions.store

import android.content.Context

class UserStore {
    companion object {
        private const val PREF_NAME = "user_pref"
        private const val PREF_KEY_USERNAME = "username"
        private const val PREF_KEY_DEVICE_TOKEN = "device_token"

        fun saveUserName(context: Context, username: String?) {
            val editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            editor.putString(PREF_KEY_USERNAME, username)
            editor.apply()
        }

        fun getUserName(context: Context): String? {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(PREF_KEY_USERNAME, "")
        }

        fun saveDeviceToken(context: Context, token: String?) {
            val editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            editor.putString(PREF_KEY_DEVICE_TOKEN, token)
            editor.apply()
        }

        fun getDeviceToken(context: Context): String? {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(PREF_KEY_DEVICE_TOKEN, null)
        }
    }
}