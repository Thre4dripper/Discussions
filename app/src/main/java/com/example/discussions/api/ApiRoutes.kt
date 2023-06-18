package com.example.discussions.api

class ApiRoutes {
    companion object {
                const val BASE_URL = "http://192.168.0.108:8000"
//        const val BASE_URL = "https://nexusdiscuss.onrender.com"

        const val LOGIN = "/api/users/login/"
        const val REGISTER = "/api/users/register/"
        const val USER_DETAILS = "/api/users/details/"
        const val USER_DETAILS_UPDATE = "/api/users/profile/update/"
        const val USER_PROFILE = "/api/users/profile/"
        const val USER_USERNAME_IMAGE = "/api/users/usernameImage/"

        // POST
        const val POST_CREATE = "/api/post/create/"
        const val POST_GET_POSTS = "/api/post/getPosts/"//todo pagination
        const val POST_GET_USER_POSTS = "/api/post/getUserPosts/"//todo pagination
        const val POST_GET_POST_BY_ID = "/api/post/getPost/"
        const val POST_DELETE_POST = "/api/post/delete/"
        const val POST_UPDATE_POST = "/api/post/update/"
        const val POST_LIKE = "/api/post/like/"

        // POLL
        const val POLL_CREATE = "/api/poll/create/"
        const val POLL_GET_USER_POSTS = "/api/poll/getUserPolls/"//todo pagination
        const val POLL_GET_POLL_BY_ID = "/api/poll/getPoll/"
        const val POLL_DELETE_POLL = "/api/poll/deletePoll/"
        const val POLL_VOTE = "/api/poll/vote/"
        const val POLL_LIKE = "/api/post/like/"

        const val DELETE_IMAGE = "/api/users/deleteimage/"

        //COMMENTS
        const val COMMENTS_GET_COMMENTS = "/api/comment/get/"//todo pagination
        const val COMMENTS_CREATE_COMMENT = "/api/comment/create/"
        const val COMMENTS_DELETE_COMMENT = "/api/comment/delete/"
        const val COMMENTS_UPDATE_COMMENT = "/api/comment/update/"
        const val COMMENTS_LIKE = "/api/post/like/"

        //NOTIFICATIONS
        const val NOTIFICATIONS_GET_ALL = "/api/notification/get/"
        const val NOTIFICATION_DELETE_BY_ID = "/api/notification/delete/"
        const val NOTIFICATION_DELETE_ALL = "/api/notification/delete_all/"
        const val NOTIFICATION_MARK_AS_READ = "/api/notification/set/"

        //DISCUSSIONS
        const val DISCUSSIONS_GET_ALL = "/api/discussion/get/"
        const val DISCUSSIONS_LIMIT = 5
    }
}