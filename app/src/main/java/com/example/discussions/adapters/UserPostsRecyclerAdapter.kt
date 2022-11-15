package com.example.discussions.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.discussions.R
import com.example.discussions.databinding.ItemDiscussionPostBinding
import com.example.discussions.models.PostModel

class UserPostsRecyclerAdapter :
    ListAdapter<PostModel, UserPostsRecyclerAdapter.UserPostsViewHolder>(UserPostsDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPostsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_discussion_post, parent, false)

        return UserPostsViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserPostsViewHolder, position: Int) {

    }

    class UserPostsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = DataBindingUtil.bind<ItemDiscussionPostBinding>(itemView)!!
    }

    class UserPostsDiffCallback : DiffUtil.ItemCallback<PostModel>() {
        override fun areItemsTheSame(oldItem: PostModel, newItem: PostModel) =
            oldItem.postId == newItem.postId

        override fun areContentsTheSame(oldItem: PostModel, newItem: PostModel) =
            oldItem == newItem
    }
}