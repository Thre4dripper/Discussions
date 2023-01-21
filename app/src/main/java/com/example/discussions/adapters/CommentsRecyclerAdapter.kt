package com.example.discussions.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.discussions.R
import com.example.discussions.databinding.ItemCommentBinding
import com.example.discussions.models.CommentModel

class CommentsRecyclerAdapter : ListAdapter<CommentModel, ViewHolder>(CommentsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = getItem(position)
        (holder as CommentViewHolder).bind(comment, itemCount)
    }

    class CommentViewHolder(itemView: View) : ViewHolder(itemView) {
        private val TAG = "CommentsRecyclerAdapter"
        val binding = DataBindingUtil.bind<ItemCommentBinding>(itemView)!!

        fun bind(commentModel: CommentModel, listSize: Int) {

            Glide.with(itemView.context).load(commentModel.userImage)
                .placeholder(R.drawable.ic_profile).circleCrop().into(binding.itemCommentUserImage)

            val avatarSize = if (commentModel.parentCommentId == null)
                dpToFloat(itemView.context, 50f)
            else
                dpToFloat(itemView.context, 30f)

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

            binding.itemCommentRepliesRv.apply {
                adapter = CommentsRecyclerAdapter()
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