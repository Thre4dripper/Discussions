package com.example.discussions.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.discussions.R
import com.example.discussions.models.PostModel

class ProfileRecyclerAdapter :
    ListAdapter<PostModel, ProfileRecyclerAdapter.ProfilePostsViewHolder>(ProfileDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_post, parent, false)

        return ProfilePostsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfilePostsViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }

    class ProfilePostsViewHolder(itemView: View) : ViewHolder(itemView) {
        fun bind(postModel: PostModel) {

        }
    }

    class ProfileDiffCallback : DiffUtil.ItemCallback<PostModel>() {
        override fun areItemsTheSame(oldItem: PostModel, newItem: PostModel) =
            oldItem.postId == newItem.postId

        override fun areContentsTheSame(oldItem: PostModel, newItem: PostModel) = oldItem == newItem
    }

}