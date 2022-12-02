package com.example.discussions.adapters

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.adapters.interfaces.LikeCommentInterface
import com.example.discussions.adapters.interfaces.PollClickInterface
import com.example.discussions.databinding.ItemDiscussionPollBinding
import com.example.discussions.models.PollModel
import com.example.discussions.ui.ZoomImageActivity
import java.text.SimpleDateFormat
import java.util.*

class PollsRecyclerAdapter(
    private var pollClickInterface: PollClickInterface,
    private var likeCommentInterface: LikeCommentInterface
) :
    ListAdapter<PollModel, ViewHolder>(PollsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_discussion_poll, parent, false)
        return PollViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val poll = getItem(position)
        (holder as PollViewHolder).bind(
            holder.binding,
            poll,
            pollClickInterface,
            likeCommentInterface
        )
    }

    class PollViewHolder(itemView: View) : ViewHolder(itemView) {
        private val TAG = "PollsRecyclerAdapter"

        val binding = DataBindingUtil.bind<ItemDiscussionPollBinding>(itemView)!!
        private val pollOptionsTvList = mutableListOf(
            binding.itemPollOption1Tv,
            binding.itemPollOption2Tv,
            binding.itemPollOption3Tv,
            binding.itemPollOption4Tv,
            binding.itemPollOption5Tv,
            binding.itemPollOption6Tv,
        )

        private val pollOptionsResultLayoutList = mutableListOf(
            binding.itemPollOption1Ll,
            binding.itemPollOption2Ll,
            binding.itemPollOption3Ll,
            binding.itemPollOption4Ll,
            binding.itemPollOption5Ll,
            binding.itemPollOption6Ll,
        )

        private val pollOptionsVotesTvList = mutableListOf(
            binding.itemPollOption1Votes,
            binding.itemPollOption2Votes,
            binding.itemPollOption3Votes,
            binding.itemPollOption4Votes,
            binding.itemPollOption5Votes,
            binding.itemPollOption6Votes,
        )

        private val pollOptionsProgressList = mutableListOf(
            binding.itemPollOption1Progress,
            binding.itemPollOption2Progress,
            binding.itemPollOption3Progress,
            binding.itemPollOption4Progress,
            binding.itemPollOption5Progress,
            binding.itemPollOption6Progress,
        )

        fun bind(
            binding: ItemDiscussionPollBinding,
            pollModel: PollModel,
            pollClickInterface: PollClickInterface,
            likeCommentInterface: LikeCommentInterface
        ) {
            //poll delete button
            binding.itemPollDeleteBtn.setOnClickListener {
                pollClickInterface.onPollDelete(pollModel.pollId)
            }

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

            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = dateFormat.parse(pollModel.createdAt)

            binding.itemPollTime.text = DateUtils.getRelativeTimeSpanString(
                date!!.time,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )

            binding.itemPollTitle.apply {
                text = pollModel.title
                visibility = if (pollModel.title.isEmpty()) View.GONE else View.VISIBLE
            }
            binding.itemPollContent.apply {
                text = pollModel.content
                visibility = if (pollModel.content.isEmpty()) View.GONE else View.VISIBLE
            }


            //hiding loading progress bar and showing poll options
            binding.itemPollLottieLoading.visibility =
                if (pollModel.isVoting) View.VISIBLE else View.GONE
            binding.itemPollOptionsLl.foreground =
                if (pollModel.isVoting) ColorDrawable(Color.WHITE) else ColorDrawable(Color.TRANSPARENT)


            //setting the poll options
            val pollOptions = pollModel.pollOptions
            val maxVotes = pollOptions.maxOf { it.votes }

            for (i in pollOptions.indices) {

                //set poll option text
                pollOptionsTvList[i].apply {
                    text = pollOptions[i].content
                    visibility = View.VISIBLE

                    //setting start drawable in text view
                    setCompoundDrawablesWithIntrinsicBounds(
                        //checking if the current user has voted for this option
                        //AND
                        //checking if any votedBy list contains the current user's username
                        if (pollModel.isVoted && pollOptions[i].votedBy.any { it.username == pollModel.username }) {
                            ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.ic_circle_checked,
                                null
                            )
                        } else {
                            ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.ic_circle_unchecked,
                                null
                            )
                        },
                        null, null, null
                    )


                    setOnClickListener {
                        //checking if the current user has already voted
                        if (!pollModel.isVoted)
                            pollClickInterface.onPollVote(pollModel.pollId, pollOptions[i].id)
                    }
                }

                //result layout visibility
                pollOptionsResultLayoutList[i].visibility =
                    if (pollModel.isVoted) View.VISIBLE else View.GONE

                if (pollModel.isVoted) {
                    //setting votes percentage
                    pollOptionsVotesTvList[i].text =
                        String.format(
                            "%d%%",
                            (pollOptions[i].votes * 100) / pollModel.totalVotes
                        )

                    //setting votes progress
                    pollOptionsProgressList[i].apply {
                        max = maxVotes
                        progress = pollOptions[i].votes
                    }
                }
            }

            //view results button
            binding.itemPollViewResultsBtn.apply {
                visibility =
                    if (pollModel.pollOptions.any { it.votedBy.isNotEmpty() } && pollModel.isVoted) View.VISIBLE else View.GONE
                setOnClickListener {
                    pollClickInterface.onPollResult(pollModel.pollId)
                }
            }

            binding.itemPollLike.apply {
                setOnClickListener {
                    likeCommentInterface.onLike(pollModel.pollId)
                }
                setCompoundDrawablesWithIntrinsicBounds(
                    if (pollModel.isLiked) {
                        R.drawable.ic_like_filled
                    } else R.drawable.ic_like,
                    0,
                    0,
                    0
                )
            }

            //setting the poll likes and comments
            binding.itemPollLikes.text = pollModel.likes.toString()

            binding.itemPollComment.apply {
                setOnClickListener {
                    likeCommentInterface.onComment(pollModel.pollId)
                }
                visibility = if (pollModel.allowComments) View.VISIBLE else View.GONE
            }
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