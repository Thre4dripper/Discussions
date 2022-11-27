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
import com.example.discussions.databinding.ItemPollVotedByBinding
import com.example.discussions.models.PollVotedByModel

class PollResultsRecyclerAdapter :
    ListAdapter<PollVotedByModel, ViewHolder>(
        PollResultsDiffUtil()
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollResultsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_poll_voted_by, parent, false)

        return PollResultsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val votedBy = getItem(position)
        (holder as PollResultsViewHolder).bind(holder.binding, votedBy)
    }

    class PollResultsViewHolder(itemView: View) : ViewHolder(itemView) {
        val binding = DataBindingUtil.bind<ItemPollVotedByBinding>(itemView)!!

        fun bind(binding: ItemPollVotedByBinding, pollVotedByModel: PollVotedByModel) {
            Glide.with(itemView.context)
                .load(pollVotedByModel.userImage)
                .circleCrop()
                .placeholder(R.drawable.ic_profile)
                .into(binding.itemVotedByProfileIv)

            binding.itemVotedByUsernameTv.text = pollVotedByModel.username
        }
    }

    class PollResultsDiffUtil : DiffUtil.ItemCallback<PollVotedByModel>() {
        override fun areItemsTheSame(
            oldItem: PollVotedByModel,
            newItem: PollVotedByModel
        ) = oldItem.userId == newItem.userId

        override fun areContentsTheSame(
            oldItem: PollVotedByModel,
            newItem: PollVotedByModel
        ) = oldItem == newItem
    }
}