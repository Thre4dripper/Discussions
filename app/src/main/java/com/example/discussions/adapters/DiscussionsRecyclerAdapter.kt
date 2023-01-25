package com.example.discussions.adapters

import android.content.Intent
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.adapters.interfaces.LikeCommentInterface
import com.example.discussions.databinding.ItemDiscussionPostBinding
import com.example.discussions.models.PostModel
import com.example.discussions.ui.ZoomImageActivity
import java.text.SimpleDateFormat
import java.util.*

class DiscussionsRecyclerAdapter(private var likeCommentInterface: LikeCommentInterface) :
    ListAdapter<PostModel, ViewHolder>(DiscussionDiffCallback()) {
    private val TAG = "DiscussionsRecyclerAdap"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_discussion_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = getItem(position)
        (holder as PostViewHolder).bind(holder.binding, post, likeCommentInterface)
    }

    class PostViewHolder(itemView: View) : ViewHolder(itemView) {
        var binding = DataBindingUtil.bind<ItemDiscussionPostBinding>(itemView)!!

        fun bind(
            binding: ItemDiscussionPostBinding,
            postModel: PostModel,
            likeCommentInterface: LikeCommentInterface
        ) {

            //hiding more options button on discussion posts
            binding.postsMoreOptions.visibility = View.GONE

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

            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = dateFormat.parse(postModel.createdAt)

            binding.itemPostTime.text = DateUtils.getRelativeTimeSpanString(
                date!!.time,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )

            binding.itemPostTitle.apply {
                text = postModel.title
                visibility = if (postModel.title.isEmpty()) View.GONE else View.VISIBLE
            }
            binding.itemPostContent.apply {
                text = postModel.content
                visibility = if (postModel.content.isEmpty()) View.GONE else View.VISIBLE
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

            //setting the post likes and comments count
            binding.itemPostLikesCount.text = postModel.likes.toString()
            binding.itemPostCommentsCount.text = postModel.comments.toString()

            //local variable for realtime like button change
            var postIsLiked = postModel.isLiked
            //setting like and comment button click listeners
            binding.itemPostLikeBtn.apply {
                setOnClickListener {
                    likeCommentInterface.onLike(postModel.postId, postModel.isLiked, postIsLiked)
                    //changing the like button icon every time it is clicked
                    postIsLiked = !postIsLiked
                    setCompoundDrawablesWithIntrinsicBounds(
                        if (postIsLiked) {
                            R.drawable.ic_like_filled
                        } else R.drawable.ic_like,
                        0,
                        0,
                        0
                    )
                    //changing the likes count every time the like button is clicked based on the current state of the post
                    binding.itemPostLikesCount.text =
                        if (postIsLiked) {
                            binding.itemPostLikesCount.text.toString().toInt().plus(1).toString()
                        } else {
                            binding.itemPostLikesCount.text.toString().toInt().minus(1).toString()
                        }
                }
                //checking if the current user has liked the post
                setCompoundDrawablesWithIntrinsicBounds(
                    if (postIsLiked) {
                        R.drawable.ic_like_filled
                    } else R.drawable.ic_like,
                    0,
                    0,
                    0
                )
            }

            binding.itemPostCommentBtn.apply {
                setOnClickListener {
                    likeCommentInterface.onComment(postModel.postId, Constants.COMMENT_TYPE_POST)
                }
                visibility = if (postModel.allowComments) View.VISIBLE else View.GONE
            }

        }
    }

    class DiscussionDiffCallback : DiffUtil.ItemCallback<PostModel>() {
        override fun areItemsTheSame(oldItem: PostModel, newItem: PostModel) =
            oldItem.postId == newItem.postId

        override fun areContentsTheSame(oldItem: PostModel, newItem: PostModel) =
            oldItem == newItem
    }
}