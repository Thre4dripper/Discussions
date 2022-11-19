package com.example.discussions.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.discussions.R
import com.example.discussions.databinding.ItemPollOptionBinding
import com.example.discussions.models.PollOptionModel

class PollOptionsRecyclerAdapter :
    ListAdapter<PollOptionModel, PollOptionsRecyclerAdapter.PollOptionViewHolder>(PollOptionDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollOptionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_poll_option, parent, false)
        return PollOptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PollOptionViewHolder, position: Int) {

    }

    class PollOptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = DataBindingUtil.bind<ItemPollOptionBinding>(itemView)!!
    }

    class PollOptionDiffUtil : DiffUtil.ItemCallback<PollOptionModel>() {
        override fun areItemsTheSame(oldItem: PollOptionModel, newItem: PollOptionModel) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: PollOptionModel,
            newItem: PollOptionModel
        ) = oldItem == newItem

    }

}