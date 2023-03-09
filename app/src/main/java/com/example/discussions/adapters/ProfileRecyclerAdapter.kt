package com.example.discussions.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.example.discussions.R
import com.example.discussions.adapters.interfaces.PostClickInterface
import com.example.discussions.databinding.ItemUserPostBinding
import com.example.discussions.models.DiscussionModel
import com.example.discussions.models.PostModel

class ProfileRecyclerAdapter(private val postClickInterface: PostClickInterface) :
    ListAdapter<DiscussionModel, ProfileRecyclerAdapter.ProfilePostsViewHolder>(ProfileDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_post, parent, false)

        return ProfilePostsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfilePostsViewHolder, position: Int) {
        val discussionPost = getItem(position).post!!
        holder.bind(holder.binding, discussionPost, postClickInterface)
    }

    class ProfilePostsViewHolder(itemView: View) : ViewHolder(itemView) {

        var binding = DataBindingUtil.bind<ItemUserPostBinding>(itemView)!!

        fun bind(
            binding: ItemUserPostBinding,
            postModel: PostModel,
            postClickInterface: PostClickInterface
        ) {
            binding.itemUserPostTitle.apply {
                text = postModel.title
                visibility = if (postModel.title.isEmpty()) View.GONE else View.VISIBLE
            }
            binding.itemUserPostContent.apply {
                text = postModel.content
                visibility = if (postModel.content.isEmpty()) View.GONE else View.VISIBLE
            }

            //blur on photos only shown on posts which have content
            if (postModel.title.isEmpty() && postModel.content.isEmpty()) {
                binding.itemUserPostImage.foreground = null
            } else {
                binding.itemUserPostImage.foreground = ContextCompat.getDrawable(
                    binding.root.context,
                    R.drawable.item_user_post_grad
                )
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

            binding.itemUserPostCv.setOnClickListener {
                postClickInterface.onPostClick(postModel.postId)
            }
        }
    }

    class ProfileDiffCallback : DiffUtil.ItemCallback<DiscussionModel>() {
        override fun areItemsTheSame(oldItem: DiscussionModel, newItem: DiscussionModel) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: DiscussionModel, newItem: DiscussionModel) = oldItem == newItem
    }

}