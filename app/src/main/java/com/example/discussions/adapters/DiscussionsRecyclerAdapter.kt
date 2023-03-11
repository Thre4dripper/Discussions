package com.example.discussions.adapters

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.example.discussions.Constants
import com.example.discussions.MyApplication
import com.example.discussions.R
import com.example.discussions.adapters.interfaces.DiscussionMenuInterface
import com.example.discussions.adapters.interfaces.LikeCommentInterface
import com.example.discussions.adapters.interfaces.PollClickInterface
import com.example.discussions.adapters.interfaces.PostClickInterface
import com.example.discussions.databinding.ItemDiscussionPollBinding
import com.example.discussions.databinding.ItemDiscussionPostBinding
import com.example.discussions.models.DiscussionModel
import com.example.discussions.models.PollModel
import com.example.discussions.models.PostModel
import com.example.discussions.ui.ZoomImageActivity
import java.text.SimpleDateFormat
import java.util.*

class DiscussionsRecyclerAdapter(
    private var likeCommentInterface: LikeCommentInterface,
    private var postClickInterface: PostClickInterface?,
    private var pollClickInterface: PollClickInterface?,
    private var discussionMenuInterface: DiscussionMenuInterface
) :
    ListAdapter<DiscussionModel, ViewHolder>(DiscussionsDiffCallback()) {

    companion object {
        const val DISCUSSION_TYPE_POST = 100
        const val DISCUSSION_TYPE_POLL = 101
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            DISCUSSION_TYPE_POST -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_discussion_post, parent, false)
                PostViewHolder(view)
            }
            DISCUSSION_TYPE_POLL -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_discussion_poll, parent, false)
                PollViewHolder(view)
            }
            else -> null!!
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val discussion = getItem(position)
        when (holder.itemViewType) {
            DISCUSSION_TYPE_POST -> {
                (holder as PostViewHolder).bind(
                    holder.binding,
                    discussion.post!!,
                    likeCommentInterface,
                    postClickInterface!!,
                    discussionMenuInterface
                )
            }
            DISCUSSION_TYPE_POLL -> {
                (holder as PollViewHolder).bind(
                    holder.binding,
                    holder.pollOptionsTvList,
                    holder.pollOptionsResultLayoutList,
                    holder.pollOptionsVotesTvList,
                    holder.pollOptionsProgressList,
                    discussion.poll!!,
                    pollClickInterface!!,
                    likeCommentInterface,
                    discussionMenuInterface
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return item.type
    }

    inner class PostViewHolder(itemView: View) : ViewHolder(itemView) {
        var binding = DataBindingUtil.bind<ItemDiscussionPostBinding>(itemView)!!

        fun bind(
            binding: ItemDiscussionPostBinding,
            postModel: PostModel,
            likeCommentInterface: LikeCommentInterface,
            postClickInterface: PostClickInterface,
            discussionMenuInterface: DiscussionMenuInterface
        ) {

            //setting up the more options menu
            binding.itemPostMenuOptions.setOnClickListener {
                discussionMenuInterface.onPostMenuClicked(postModel)
            }

            //setting the profile image of current post's user
            Glide.with(itemView.context)
                .load(postModel.userImage)
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.itemPostUserImage)

            //navigating to zoom image activity on clicking profile image
            binding.itemPostUserImage.setOnClickListener {
                val context = binding.itemPostUserImage.context
                val intent = Intent(context, ZoomImageActivity::class.java)
                intent.putExtra(Constants.ZOOM_IMAGE_URL, postModel.userImage)
                context.startActivity(intent)
            }

            binding.itemPostUsername.text = postModel.username

            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = dateFormat.parse(postModel.createdAt)

            binding.itemPostTime.text = DateUtils.getRelativeTimeSpanString(
                date!!.time,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )

            binding.itemPostTitle.apply {
                text = postModel.title
                visibility = if (postModel.title.isEmpty()) View.GONE else View.VISIBLE
            }
            binding.itemPostContent.apply {
                text = postModel.content
                visibility = if (postModel.content.isEmpty()) View.GONE else View.VISIBLE
            }

            val image = postModel.postImage
            if (image != "") {
                binding.itemPostImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(image)
                    .override(Target.SIZE_ORIGINAL)
                    .into(binding.itemPostImage)
                binding.itemPostImage.setOnClickListener {
                    val context = binding.itemPostImage.context
                    val intent = Intent(context, ZoomImageActivity::class.java)
                    intent.putExtra(Constants.ZOOM_IMAGE_URL, image)
                    context.startActivity(intent)
                }
            } else {
                binding.itemPostImage.visibility = View.GONE
            }

            //setting the post likes and comments count
            binding.itemPostLikesCount.text = postModel.likes.toString()
            binding.itemPostCommentsCount.text = postModel.comments.toString()

            //local variable for realtime like button change
            var postIsLiked = postModel.isLiked
            //setting like and comment button click listeners
            binding.itemPostLikeBtn.apply {
                setOnClickListener {
                    likeCommentInterface.onPostLike(
                        postModel.postId,
                        postModel.isLiked,
                        postIsLiked
                    )
                    //changing the like button icon every time it is clicked
                    postIsLiked = !postIsLiked
                    setCompoundDrawablesWithIntrinsicBounds(
                        if (postIsLiked) {
                            R.drawable.ic_like_filled
                        } else R.drawable.ic_like,
                        0,
                        0,
                        0
                    )
                    //changing the likes count every time the like button is clicked based on the current state of the post
                    binding.itemPostLikesCount.text =
                        if (postIsLiked) {
                            binding.itemPostLikesCount.text.toString().toInt().plus(1).toString()
                        } else {
                            binding.itemPostLikesCount.text.toString().toInt().minus(1).toString()
                        }
                }
                //checking if the current user has liked the post
                setCompoundDrawablesWithIntrinsicBounds(
                    if (postIsLiked) {
                        R.drawable.ic_like_filled
                    } else R.drawable.ic_like,
                    0,
                    0,
                    0
                )
            }

            binding.itemPostCommentBtn.apply {
                setOnClickListener {
                    likeCommentInterface.onPostComment(postModel.postId)
                }
                visibility = if (postModel.allowComments) View.VISIBLE else View.GONE
            }

            binding.itemPostUserInfoCl.setOnClickListener {
                postClickInterface.onPostClick(postModel.postId)
            }
        }
    }

    inner class PollViewHolder(itemView: View) : ViewHolder(itemView) {
        private val TAG = "PollsRecyclerAdapter"

        val binding = DataBindingUtil.bind<ItemDiscussionPollBinding>(itemView)!!
        val pollOptionsTvList = mutableListOf(
            binding.itemPollOption1Tv,
            binding.itemPollOption2Tv,
            binding.itemPollOption3Tv,
            binding.itemPollOption4Tv,
            binding.itemPollOption5Tv,
            binding.itemPollOption6Tv,
        )

        val pollOptionsResultLayoutList = mutableListOf(
            binding.itemPollOption1Ll,
            binding.itemPollOption2Ll,
            binding.itemPollOption3Ll,
            binding.itemPollOption4Ll,
            binding.itemPollOption5Ll,
            binding.itemPollOption6Ll,
        )

        val pollOptionsVotesTvList = mutableListOf(
            binding.itemPollOption1Votes,
            binding.itemPollOption2Votes,
            binding.itemPollOption3Votes,
            binding.itemPollOption4Votes,
            binding.itemPollOption5Votes,
            binding.itemPollOption6Votes,
        )

        val pollOptionsProgressList = mutableListOf(
            binding.itemPollOption1Progress,
            binding.itemPollOption2Progress,
            binding.itemPollOption3Progress,
            binding.itemPollOption4Progress,
            binding.itemPollOption5Progress,
            binding.itemPollOption6Progress,
        )

        fun bind(
            binding: ItemDiscussionPollBinding,
            pollOptionsTvList: List<TextView>,
            pollOptionsResultLayoutList: List<LinearLayout>,
            pollOptionsVotesTvList: List<TextView>,
            pollOptionsProgressList: List<ProgressBar>,
            pollModel: PollModel,
            pollClickInterface: PollClickInterface,
            likeCommentInterface: LikeCommentInterface,
            discussionMenuInterface: DiscussionMenuInterface
        ) {
            //setting up the more options menu
            binding.itemPollMenuOptions.setOnClickListener {
                discussionMenuInterface.onPollMenuClicked(pollModel)
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

            //hiding all the poll options first
            for (i in 0 until 6) {
                pollOptionsTvList[i].visibility = View.GONE
                pollOptionsResultLayoutList[i].visibility = View.GONE
            }

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
                        if (pollModel.isVoted && pollOptions[i].votedBy.any { it.username == "@${MyApplication.username}" }) {
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

            binding.itemPollPrivacyIcon.apply {
                visibility =
                    if (pollModel.isPrivate && pollModel.username != "@${MyApplication.username}") View.VISIBLE else View.GONE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    tooltipText = "Private poll, only creator can see the stats"
                }
                setOnClickListener {
                    binding.itemPollPrivacyIcon.performLongClick()
                }
            }

            //view results button
            binding.itemPollViewResultsBtn.apply {
                visibility = if (pollModel.isVoted) {
                    if (pollModel.isPrivate && pollModel.username != "@${MyApplication.username}")
                        View.GONE
                    else View.VISIBLE
                } else View.GONE
                setOnClickListener {
                    pollClickInterface.onPollResult(pollModel.pollId)
                }
            }

            //setting the poll likes and comments count
            binding.itemPollLikesCount.text = pollModel.likes.toString()
            binding.itemPollCommentsCount.text = pollModel.comments.toString()

            //local variable for realtime like button change
            var pollIsLiked = pollModel.isLiked
            //setting like and comment button click listeners
            binding.itemPollLikeBtn.apply {
                setOnClickListener {
                    likeCommentInterface.onPollLike(
                        pollModel.pollId,
                        pollModel.isLiked,
                        pollIsLiked
                    )
                    //changing the like button icon every time it is clicked
                    pollIsLiked = !pollIsLiked
                    setCompoundDrawablesWithIntrinsicBounds(
                        if (pollIsLiked) {
                            R.drawable.ic_like_filled
                        } else R.drawable.ic_like,
                        0,
                        0,
                        0
                    )
                    //changing the likes count every time the like button is clicked based on the current state of the post
                    binding.itemPollLikesCount.text =
                        if (pollIsLiked) {
                            binding.itemPollLikesCount.text.toString().toInt().plus(1).toString()
                        } else {
                            binding.itemPollLikesCount.text.toString().toInt().minus(1).toString()
                        }
                }
                //checking if the current user has liked the post
                setCompoundDrawablesWithIntrinsicBounds(
                    if (pollIsLiked) {
                        R.drawable.ic_like_filled
                    } else R.drawable.ic_like,
                    0,
                    0,
                    0
                )
            }

            binding.itemPollCommentBtn.apply {
                setOnClickListener {
                    likeCommentInterface.onPollComment(pollModel.pollId)
                }
                visibility = if (pollModel.allowComments) View.VISIBLE else View.GONE
            }

            binding.itemPollUserInfoCl.setOnClickListener {
                pollClickInterface.onPollClick(pollModel.pollId)
            }

        }
    }

    class DiscussionsDiffCallback : DiffUtil.ItemCallback<DiscussionModel>() {
        override fun areItemsTheSame(oldItem: DiscussionModel, newItem: DiscussionModel) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: DiscussionModel, newItem: DiscussionModel) =
            oldItem == newItem
    }
}