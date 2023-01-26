package com.example.discussions.adapters

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.discussions.R
import com.example.discussions.adapters.interfaces.CommentInterface
import com.example.discussions.databinding.ItemCommentBinding
import com.example.discussions.models.CommentModel
import java.text.SimpleDateFormat
import java.util.*

class CommentsRecyclerAdapter(private var commentInterface: CommentInterface) :
    ListAdapter<CommentModel, ViewHolder>(CommentsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = getItem(position)
        (holder as CommentViewHolder).bind(comment, itemCount, commentInterface)
    }

    class CommentViewHolder(itemView: View) : ViewHolder(itemView) {
        private val TAG = "CommentsRecyclerAdapter"
        val binding = DataBindingUtil.bind<ItemCommentBinding>(itemView)!!

        fun bind(commentModel: CommentModel, listSize: Int, commentInterface: CommentInterface) {

            Glide.with(itemView.context).load(commentModel.userImage)
                .placeholder(R.drawable.ic_profile).circleCrop().into(binding.itemCommentUserImage)

            val avatarSize =
                if (commentModel.parentCommentId == null) dpToFloat(itemView.context, 40f)
                else dpToFloat(itemView.context, 25f)

            binding.horizontalLine.visibility =
                if (commentModel.parentCommentId != null) View.VISIBLE else View.GONE
            binding.verticalLine1.visibility =
                if (commentModel.parentCommentId != null) View.VISIBLE else View.GONE
            binding.verticalLine2.visibility =
                if (commentModel.parentCommentId != null && adapterPosition < listSize - 1) View.VISIBLE else View.GONE
            binding.verticalLine3.visibility =
                if (commentModel.replies.isNotEmpty()) View.VISIBLE else View.GONE

            binding.itemCommentUserImage.layoutParams.width = avatarSize
            binding.itemCommentUserImage.layoutParams.height = avatarSize

            binding.itemCommentUserName.text = commentModel.username
            binding.itemCommentContent.text = commentModel.comment

            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = dateFormat.parse(commentModel.createdAt)

            binding.itemCommentTimeTv.text = DateUtils.getRelativeTimeSpanString(
                date!!.time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
            )
            binding.itemCommentLikeTv.text = if (commentModel.isLiked) "Liked" else "Like"

            binding.itemCommentLikeCountTv.text = commentModel.likes.toString()
            binding.itemCommentLikeCountTv.visibility =
                if (commentModel.likes > 0) View.VISIBLE else View.GONE

            //local variable for realtime like button change
            var commentLikeStatus = commentModel.isLiked

            binding.itemCommentLikeTv.setOnClickListener {
                commentInterface.onCommentLikeChanged(
                    commentModel.commentId, commentModel.isLiked, commentLikeStatus
                )
                //changing the like button status
                commentLikeStatus = !commentLikeStatus
                //changing the like button text
                binding.itemCommentLikeTv.text = if (commentLikeStatus) "Liked" else "Like"

                //changing the like count
                if (commentLikeStatus) {
                    binding.itemCommentLikeCountTv.text =
                        binding.itemCommentLikeCountTv.text.toString().toInt().plus(1).toString()
                } else {
                    binding.itemCommentLikeCountTv.text =
                        binding.itemCommentLikeCountTv.text.toString().toInt().minus(1).toString()
                }

                //changing the like count visibility
                binding.itemCommentLikeCountTv.visibility =
                    if (binding.itemCommentLikeCountTv.text.toString()
                            .toInt() > 0
                    ) View.VISIBLE else View.GONE
            }

            binding.itemCommentReplyTv.setOnClickListener {
                commentInterface.onCommentReply(commentModel.commentId, commentModel.username)
            }

            //for opening the comment options menu
            binding.itemCommentCv.setOnLongClickListener {
                commentInterface.onCommentLongClick(commentModel)
                true
            }

            binding.itemCommentRepliesRv.apply {
                adapter = CommentsRecyclerAdapter(commentInterface)
                (adapter as CommentsRecyclerAdapter).submitList(commentModel.replies)
            }
        }

        private fun dpToFloat(context: Context, dp: Float): Int {
            return (dp * context.resources.displayMetrics.density).toInt()
        }
    }

    class CommentsDiffCallback : DiffUtil.ItemCallback<CommentModel>() {
        override fun areItemsTheSame(oldItem: CommentModel, newItem: CommentModel) =
            oldItem.commentId == newItem.commentId

        override fun areContentsTheSame(oldItem: CommentModel, newItem: CommentModel) =
            oldItem == newItem
    }
}