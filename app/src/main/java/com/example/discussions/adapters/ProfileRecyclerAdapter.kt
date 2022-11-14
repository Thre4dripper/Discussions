package com.example.discussions.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
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
        holder.bind(holder.binding, post)
    }

    class ProfilePostsViewHolder(itemView: View) : ViewHolder(itemView) {

        var binding = DataBindingUtil.bind<ItemUserPostBinding>(itemView)!!

        fun bind(binding: ItemUserPostBinding, postModel: PostModel) {
            binding.itemUserPostTitle.apply {
                text = postModel.title
                visibility = if (postModel.title.isEmpty()) View.GONE else View.VISIBLE
            }
            binding.itemUserPostContent.apply {
                text = postModel.content
                visibility = if (postModel.content.isEmpty()) View.GONE else View.VISIBLE
            }

            val image = postModel.postImage
            if (image != "") {
                binding.itemUserPostImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(image)
                    .override(Target.SIZE_ORIGINAL)
                    .into(binding.itemUserPostImage)
            } else {
                binding.itemUserPostImage.visibility = View.GONE
            }
        }
    }

    class ProfileDiffCallback : DiffUtil.ItemCallback<PostModel>() {
        override fun areItemsTheSame(oldItem: PostModel, newItem: PostModel) =
            oldItem.postId == newItem.postId

        override fun areContentsTheSame(oldItem: PostModel, newItem: PostModel) = oldItem == newItem
    }

}