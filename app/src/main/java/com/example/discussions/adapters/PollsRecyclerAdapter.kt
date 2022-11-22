package com.example.discussions.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.discussions.R
import com.example.discussions.databinding.ItemDiscussionPollBinding

class PollsRecyclerAdapter : RecyclerView.Adapter<PollsRecyclerAdapter.PollViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_discussion_poll, parent, false)

        return PollViewHolder(view)
    }

    override fun onBindViewHolder(holder: PollViewHolder, position: Int) {
        holder.bind(holder.binding)
    }

    override fun getItemCount() = 10

    class PollViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = DataBindingUtil.bind<ItemDiscussionPollBinding>(itemView)!!

        fun bind(binding: ItemDiscussionPollBinding) {
            //dummy data
            binding.itemPostUsername.text = "Username"
            binding.itemPostTime.text = "11:00 AM"
            binding.itemPostTitle.text = "Programming is fun"
            binding.itemPostContent.text = "What is your favorite programming language?"
            binding.itemPollOption1.text = "C++"
            binding.itemPollOption2.text = "Java"
            binding.itemPollOption3.text = "Kotlin"
            binding.itemPollOption4.text = "Python"
            binding.itemPollOption5.text = "Javascript"
            binding.itemPollOption6.text = "C#"

            binding.itemPollOption1Votes.text = "10%"
            binding.itemPollOption2Votes.text = "20%"
            binding.itemPollOption3Votes.text = "30%"
            binding.itemPollOption4Votes.text = "40%"
            binding.itemPollOption5Votes.text = "50%"
            binding.itemPollOption6Votes.text = "60%"

            binding.itemPollOption1Progress.apply {
                max = 60
                progress = 10
            }
            binding.itemPollOption2Progress.apply {
                max = 60
                progress = 20
            }
            binding.itemPollOption3Progress.apply {
                max = 60
                progress = 30
            }
            binding.itemPollOption4Progress.apply {
                max = 60
                progress = 40
            }
            binding.itemPollOption5Progress.apply {
                max = 60
                progress = 50
            }
            binding.itemPollOption6Progress.apply {
                max = 60
                progress = 60
            }

        }

    }
}