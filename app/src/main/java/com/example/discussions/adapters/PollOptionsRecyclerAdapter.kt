package com.example.discussions.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.discussions.R
import com.example.discussions.databinding.ItemPollOptionBinding
import com.example.discussions.models.PollOptionModel

class PollOptionsRecyclerAdapter(private var pollOptionClickInterface: PollOptionClickInterface) :
    ListAdapter<PollOptionModel, PollOptionsRecyclerAdapter.PollOptionViewHolder>(PollOptionDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollOptionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_poll_option, parent, false)
        return PollOptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PollOptionViewHolder, position: Int) {
        val poll = getItem(position)
        holder.bind(poll, holder.binding, pollOptionClickInterface)
    }

    interface PollOptionClickInterface {
        fun onPollOptionDelete(position: Int)
        fun onPollTextChanged(position: Int, text: String)
    }

    class PollOptionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val binding = DataBindingUtil.bind<ItemPollOptionBinding>(itemView)!!

        fun bind(
            pollOptionModel: PollOptionModel,
            binding: ItemPollOptionBinding,
            pollOptionClickInterface: PollOptionClickInterface
        ) {
            binding.itemCreatePollOptionTil.hint = pollOptionModel.hint
            binding.itemCreatePollOptionEt.setText(pollOptionModel.content)

            //for updating poll option list when text is changed
            binding.itemCreatePollOptionEt.addTextChangedListener {
                pollOptionClickInterface.onPollTextChanged(adapterPosition, it.toString())
            }

            //for deleting poll option
            binding.itemCreatePollOptionDeleteIv.setOnClickListener {
                pollOptionClickInterface.onPollOptionDelete(adapterPosition)
            }
        }
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