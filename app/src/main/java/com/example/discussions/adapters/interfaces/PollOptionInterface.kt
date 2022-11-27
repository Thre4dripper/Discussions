package com.example.discussions.adapters.interfaces

interface PollOptionInterface {
    fun onPollOptionDelete(position: Int)
    fun onPollTextChanged(position: Int, text: String)
}