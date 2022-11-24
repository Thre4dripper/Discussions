package com.example.discussions.adapters

import android.content.Intent
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.databinding.ItemDiscussionPostBinding
import com.example.discussions.models.PostModel
import com.example.discussions.ui.ZoomImageActivity
import java.text.SimpleDateFormat
import java.util.*

class UserPostsRecyclerAdapter(private var postOptionsInterface: PostOptionsInterface) :
    ListAdapter<PostModel, UserPostsRecyclerAdapter.UserPostsViewHolder>(UserPostsDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPostsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_discussion_post, parent, false)

        return UserPostsViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserPostsViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(holder.binding, post, postOptionsInterface)
    }

    interface PostOptionsInterface {
        fun onPostEdit(postId: String)
        fun onPostDelete(postId: String)
    }

    class UserPostsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = DataBindingUtil.bind<ItemDiscussionPostBinding>(itemView)!!

        fun bind(
            binding: ItemDiscussionPostBinding,
            postModel: PostModel,
            posInterface: PostOptionsInterface
        ) {
            //setting post popup menu
            val popupMenu = PopupMenu(binding.root.context, binding.postsMoreOptions)
            popupMenu.inflate(R.menu.post_options_menu)

            binding.postsMoreOptions.setOnClickListener {
                popupMenu.show()
            }

            //setting post menu options
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.post_options_menu_edit -> {
                        posInterface.onPostEdit(postModel.postId)
                        true
                    }
                    R.id.post_options_menu_delete -> {
                        posInterface.onPostDelete(postModel.postId)
                        true
                    }
                    else -> false
                }
            }

            //setting the profile image of current post's user
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
            binding.itemPostTime.text = DateUtils.getRelativeTimeSpanString(
                time!!.time,
                System.currentTimeMillis(),
                DateUtils.SECOND_IN_MILLIS
            )

            binding.itemPostTitle.apply {
                text = postModel.title
            }
            binding.itemPostContent.apply {
                text = postModel.content
            }

            val image = postModel.postImage
            if (image != "") {
                binding.itemPostImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(image)
                    .override(Target.SIZE_ORIGINAL)
                    .into(binding.itemPostImage)
                binding.itemPostImage.setOnClickListener {
                    val context = binding.itemPostImage.context
                    val intent = Intent(context, ZoomImageActivity::class.java)
                    intent.putExtra(Constants.ZOOM_IMAGE_URL, image)
                    context.startActivity(intent)
                }
            } else {
                binding.itemPostImage.visibility = View.GONE
            }

            binding.itemPostLikes.text = postModel.likes.toString()
            binding.itemPostComments.text = postModel.comments.toString()
        }
    }

    class UserPostsDiffCallback : DiffUtil.ItemCallback<PostModel>() {
        override fun areItemsTheSame(oldItem: PostModel, newItem: PostModel) =
            oldItem.postId == newItem.postId

        override fun areContentsTheSame(oldItem: PostModel, newItem: PostModel) =
            oldItem == newItem
    }
}