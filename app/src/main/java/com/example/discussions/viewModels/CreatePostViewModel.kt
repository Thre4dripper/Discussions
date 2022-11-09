package com.example.discussions.viewModels

import androidx.lifecycle.ViewModel

class CreatePostViewModel : ViewModel() {
    var profileImage: String? = null
    var username: String? = null
    var postTitle: String? = null
    var postContent: String? = null
    var postImage: String? = null
    var allowComments: Boolean = true
}