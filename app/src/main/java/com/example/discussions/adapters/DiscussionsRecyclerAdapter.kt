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
import com.example.discussions.databinding.ItemDiscussionPostBinding
import com.example.discussions.models.PostModel
import java.text.SimpleDateFormat
import java.util.*

class DiscussionsRecyclerAdapter : ListAdapter<PostModel, ViewHolder>(DiscussionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_discussion_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = getItem(position)
        (holder as PostViewHolder).bind(post)
    }

    class PostViewHolder(itemView: View) : ViewHolder(itemView) {
        var binding: ItemDiscussionPostBinding

        init {
            binding = DataBindingUtil.bind(itemView)!!
        }

        fun bind(postModel: PostModel) {
            binding.itemPostUsername.text = postModel.createdBy

            val time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                .parse(postModel.createdAt)
            binding.itemPostTime.text = time?.let {
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    .format(it)
            }

            binding.itemPostTitle.text = postModel.title
            binding.itemPostContent.text = postModel.content

            Glide.with(itemView.context)
                .load(postModel.postImage)
                .into(binding.itemPostImage)

            binding.itemPostLikes.text = postModel.likes.toString()
            binding.itemPostComments.text = postModel.comments.toString()
        }
    }

    class DiscussionDiffCallback : DiffUtil.ItemCallback<PostModel>() {
        override fun areItemsTheSame(oldItem: PostModel, newItem: PostModel) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PostModel, newItem: PostModel) =
            oldItem == newItem
    }
}