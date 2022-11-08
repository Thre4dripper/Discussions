package com.example.discussions.api

class ApiRoutes {
    companion object {
        const val BASE_URL = "https://nexus-discuss.herokuapp.com"

        const val LOGIN = "/api/users/login/"
        const val REGISTER = "/api/users/register/"
        const val USER_DETAILS = "/api/users/details/"
        const val USER_DETAILS_UPDATE = "/api/users/profile/update/"
        const val USER_PROFILE = "/api/users/profile/"
    }
}