package com.example.discussions.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.databinding.ItemDiscussionPostBinding
import com.example.discussions.models.PostModel
import com.example.discussions.ui.zoomImage.ZoomImageActivity
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
            Glide.with(itemView.context)
                .load(postModel.userImage)
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.itemPostUserImage)

            //navigating to zoom image activity on clicking profile image
            binding.itemPostUserImage.setOnClickListener {
                val context = binding.itemPostUserImage.context
                val intent = Intent(context, ZoomImageActivity::class.java)
                intent.putExtra(Constants.ZOOM_IMAGE_URL, postModel.userImage)
                context.startActivity(intent)
            }

            binding.itemPostUsername.text = postModel.username

            val time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                .parse(postModel.createdAt)
            binding.itemPostTime.text = time?.let {
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    .format(it)
            }

            //hiding post title if it is empty
            val title = postModel.title
            if (title.isNotEmpty()) {
                binding.itemPostTitle.text = title
            } else {
                binding.itemPostTitle.visibility = View.GONE
            }

            //hiding post description if it is empty
            val content = postModel.content
            if (content.isNotEmpty()) {
                binding.itemPostContent.text = content
            } else {
                binding.itemPostContent.visibility = View.GONE
            }

            Glide.with(itemView.context)
                .load(postModel.postImage)
                .into(binding.itemPostImage)

            //navigating to zoom image activity if post image is clicked
            binding.itemPostImage.setOnClickListener {
                val context = binding.itemPostUserImage.context
                val intent = Intent(context, ZoomImageActivity::class.java)
                intent.putExtra(Constants.ZOOM_IMAGE_URL, postModel.postImage)
                context.startActivity(intent)
            }

            binding.itemPostLikes.text = postModel.likes.toString()
            binding.itemPostComments.text = postModel.comments.toString()
        }
    }

    class DiscussionDiffCallback : DiffUtil.ItemCallback<PostModel>() {
        override fun areItemsTheSame(oldItem: PostModel, newItem: PostModel) =
            oldItem.postId == newItem.postId

        override fun areContentsTheSame(oldItem: PostModel, newItem: PostModel) =
            oldItem == newItem
    }
}