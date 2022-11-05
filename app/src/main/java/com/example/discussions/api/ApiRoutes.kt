package com.example.discussions.api

class ApiRoutes {
    companion object {
        const val BASE_URL = "https://nexus-discuss.herokuapp.com"

        const val LOGIN = "/api/users/login/"
        const val REGISTER = "/api/users/register/"
        const val PROFILE = "/api/users/profile/"
        const val UPDATE_PROFILE = "/api/users/profile/update/"
    }
}