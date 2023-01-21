package com.example.discussions.adapters.commentAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.discussions.R
import com.example.discussions.databinding.ItemCommentReplyBinding
import com.example.discussions.models.ReplyCommentModel

class ReplyRecyclerAdapter : ListAdapter<ReplyCommentModel, ViewHolder>(ReplyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment_reply, parent, false)
        return ReplyViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reply = getItem(position)
        (holder as ReplyViewHolder).bind(reply)
    }

    class ReplyViewHolder(itemView: View) : ViewHolder(itemView) {
        val binding = DataBindingUtil.bind<ItemCommentReplyBinding>(itemView)!!

        fun bind(replyCommentModel: ReplyCommentModel) {
            Glide.with(itemView.context)
                .load(replyCommentModel.userImage)
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.itemCommentUserImage)

            binding.itemCommentUserName.text = replyCommentModel.username
            binding.itemCommentContent.text = replyCommentModel.reply
        }
    }

    class ReplyDiffCallback : DiffUtil.ItemCallback<ReplyCommentModel>() {
        override fun areItemsTheSame(oldItem: ReplyCommentModel, newItem: ReplyCommentModel) =
            oldItem.replyId == newItem.replyId

        override fun areContentsTheSame(oldItem: ReplyCommentModel, newItem: ReplyCommentModel) =
            oldItem == newItem
    }

}