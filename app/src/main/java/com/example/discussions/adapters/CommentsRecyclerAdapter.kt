package com.example.discussions.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.discussions.R
import com.example.discussions.models.CommentModel

class CommentsRecyclerAdapter : ListAdapter<CommentModel, ViewHolder>(CommentsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    class CommentViewHolder(itemView: View) : ViewHolder(itemView) {

    }

    class CommentsDiffCallback : DiffUtil.ItemCallback<CommentModel>() {
        override fun areItemsTheSame(oldItem: CommentModel, newItem: CommentModel) =
            oldItem.commentId == newItem.commentId

        override fun areContentsTheSame(oldItem: CommentModel, newItem: CommentModel) =
            oldItem == newItem
    }
}