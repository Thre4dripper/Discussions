package com.example.discussions.store

import android.content.Context

class LoginStore {
    companion object {
        private const val PREF_NAME = "login_pref"
        private const val PREF_KEY_JWT = "jwt"

        fun saveJWTToken(context: Context, token: String) {
            val editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            editor.putString(PREF_KEY_JWT, token)
            editor.apply()
        }

        fun getJWTToken(context: Context): String? {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(PREF_KEY_JWT, null)
        }
    }
}