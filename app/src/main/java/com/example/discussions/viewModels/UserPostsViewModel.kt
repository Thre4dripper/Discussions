package com.example.discussions.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.models.PostModel
import com.example.discussions.repositories.PostRepository

class UserPostsViewModel : ViewModel() {
    private var _userPosts = MutableLiveData<List<PostModel>>()
    val userPosts: LiveData<List<PostModel>>
        get() = _userPosts

    fun getUserPosts() {
        _userPosts.value = PostRepository.userPostsList
    }
}