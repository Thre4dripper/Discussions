package com.example.discussions.api

class ApiRoutes {
    companion object {
        const val BASE_URL = "https://nexus-discuss.herokuapp.com"

        const val LOGIN = "/api/users/login/"
        const val REGISTER = "/api/users/register/"
        const val USER_DETAILS = "/api/users/details/"
        const val USER_DETAILS_UPDATE = "/api/users/profile/update/"
        const val USER_PROFILE = "/api/users/profile/"
        const val USER_USERNAME_IMAGE = "/api/users/usernameImage/"
        const val POST_CREATE = "/api/post/create/"
        const val POST_GET_POSTS = "/api/post/getPosts/"
        const val POST_GET_USER_POSTS = "/api/post/getUserPosts/"
        const val POST_DELETE_POST = "/api/post/delete/"
        const val POST_UPDATE_POST = "/api/post/update/"
        const val POLL_CREATE = "/api/poll/create/"

        const val DELETE_IMAGE = "/api/users/deleteimage/"
    }
}