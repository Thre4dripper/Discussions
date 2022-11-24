package com.example.discussions.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.databinding.ItemDiscussionPollBinding
import com.example.discussions.models.PollModel
import com.example.discussions.ui.ZoomImageActivity
import java.text.SimpleDateFormat
import java.util.*

class PollsRecyclerAdapter : ListAdapter<PollModel, ViewHolder>(PollsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_discussion_poll, parent, false)
        return PollViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val poll = getItem(position)
        (holder as PollViewHolder).bind(holder.binding, poll)
    }


    class PollViewHolder(itemView: View) : ViewHolder(itemView) {
        val binding = DataBindingUtil.bind<ItemDiscussionPollBinding>(itemView)!!
        val pollOptionsTvList = mutableListOf(
            binding.itemPollOption1Tv,
            binding.itemPollOption2Tv,
            binding.itemPollOption3Tv,
            binding.itemPollOption4Tv,
            binding.itemPollOption5Tv,
            binding.itemPollOption6Tv,
        ).toTypedArray()

        val pollOptionsResultLayoutList = mutableListOf(
            binding.itemPollOption1Ll,
            binding.itemPollOption2Ll,
            binding.itemPollOption3Ll,
            binding.itemPollOption4Ll,
            binding.itemPollOption5Ll,
            binding.itemPollOption6Ll,
        ).toTypedArray()

        val pollOptionsVotesTvList = mutableListOf(
            binding.itemPollOption1Votes,
            binding.itemPollOption2Votes,
            binding.itemPollOption3Votes,
            binding.itemPollOption4Votes,
            binding.itemPollOption5Votes,
            binding.itemPollOption6Votes,
        ).toTypedArray()

        val pollOptionsProgressList = mutableListOf(
            binding.itemPollOption1Progress,
            binding.itemPollOption2Progress,
            binding.itemPollOption3Progress,
            binding.itemPollOption4Progress,
            binding.itemPollOption5Progress,
            binding.itemPollOption6Progress,
        ).toTypedArray()

        fun bind(binding: ItemDiscussionPollBinding, pollModel: PollModel) {
            //hiding more options button on discussion polls
            binding.pollsMoreOptions.visibility = View.GONE

            //setting the profile image of current poll's user
            Glide.with(itemView.context)
                .load(pollModel.userImage)
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.itemPollUserImage)

            //navigating to zoom image activity on clicking profile image
            binding.itemPollUserImage.setOnClickListener {
                val context = binding.itemPollUserImage.context
                val intent = Intent(context, ZoomImageActivity::class.java)
                intent.putExtra(Constants.ZOOM_IMAGE_URL, pollModel.userImage)
                context.startActivity(intent)
            }

            binding.itemPollUsername.text = pollModel.username

            val time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                .parse(pollModel.createdAt)
            binding.itemPollTime.text = time?.let {
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    .format(it)
            }

            binding.itemPollTitle.apply {
                text = pollModel.title
                visibility = if (pollModel.title.isEmpty()) View.GONE else View.VISIBLE
            }
            binding.itemPollContent.apply {
                text = pollModel.content
                visibility = if (pollModel.content.isEmpty()) View.GONE else View.VISIBLE
            }

            //setting the poll options
            val pollOptions = pollModel.pollOptions

            binding.itemPollLikes.text = pollModel.likes.toString()
            binding.itemPollComments.text = pollModel.comments.toString()
        }
    }

    class PollsDiffCallback : DiffUtil.ItemCallback<PollModel>() {
        override fun areItemsTheSame(oldItem: PollModel, newItem: PollModel) =
            oldItem.pollId == newItem.pollId

        override fun areContentsTheSame(oldItem: PollModel, newItem: PollModel) =
            oldItem == newItem
    }
}