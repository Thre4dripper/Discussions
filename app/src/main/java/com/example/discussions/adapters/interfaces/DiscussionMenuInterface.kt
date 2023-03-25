package com.example.discussions.adapters.interfaces

import com.example.discussions.models.PollModel
import com.example.discussions.models.PostModel

interface DiscussionMenuInterface {
    fun onMenuClicked(post: PostModel?, poll: PollModel?, type: Int)
    fun onMenuEdit(postId: String?, pollId: String?, type: Int)
    fun onMenuDelete(postId: String?, pollId: String?, type: Int)

    //TODO add onMenuReport
    //TODO add onMenuBookmark
}