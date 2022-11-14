package com.example.discussions.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.discussions.R
import com.example.discussions.databinding.ItemUserPostBinding
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

        var binding: ItemUserPostBinding

        init {
            binding = DataBindingUtil.bind(itemView)!!
        }

        fun bind(postModel: PostModel) {
            //hiding post title if it is empty
            val title = postModel.title
            if (title.isNotEmpty()) {
                binding.itemUserPostTitle.text = title
            } else {
                binding.itemUserPostTitle.visibility = View.GONE
            }

            //hiding post description if it is empty
            val content = postModel.content
            if (content.isNotEmpty()) {
                binding.itemUserPostContent.text = content
            } else {
                binding.itemUserPostContent.visibility = View.GONE
            }

            Glide.with(itemView.context)
                .load(postModel.postImage)
                .into(binding.itemUserPostImage)
        }
    }

    class ProfileDiffCallback : DiffUtil.ItemCallback<PostModel>() {
        override fun areItemsTheSame(oldItem: PostModel, newItem: PostModel) =
            oldItem.postId == newItem.postId

        override fun areContentsTheSame(oldItem: PostModel, newItem: PostModel) = oldItem == newItem
    }

}