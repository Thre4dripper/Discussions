package com.example.discussions.api

interface ResponseCallback {
    fun onSuccess(response: String)
    fun onError(response: String)
}