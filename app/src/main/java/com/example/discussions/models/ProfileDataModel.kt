package com.example.discussions.models

data class ProfileDataModel(
    val userId: String,
    var profileImage: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val postsCount: String,
    val pollsCount: String,
)
