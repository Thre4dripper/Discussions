package com.example.discussions.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

class LoginStore(context: Context) {
    companion object {
        const val PREF_NAME = "login_pref"
        val PREF_KEY_JWT = stringPreferencesKey("jwt_token")
    }

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREF_NAME)
    private val dataStore = context.dataStore

    suspend fun saveJWTToken(token: String) {
        dataStore.edit { preferences ->
            preferences[PREF_KEY_JWT] = token
        }
    }

    suspend fun getJWTToken(): String? {
        return dataStore.data.first()[PREF_KEY_JWT]
    }
}