package com.example.discussions.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.adapters.CommentsRecyclerAdapter
import com.example.discussions.databinding.ActivityPollDetailsBinding
import com.example.discussions.databinding.LoadingDialogBinding
import com.example.discussions.models.PollModel
import com.example.discussions.viewModels.CommentsViewModel
import com.example.discussions.viewModels.PollDetailsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class PollDetailsActivity : AppCompatActivity() {
    private val TAG = "PollDetailsActivity"

    private lateinit var binding: ActivityPollDetailsBinding
    private lateinit var viewModel: PollDetailsViewModel
    private lateinit var commentsViewModel: CommentsViewModel

    private lateinit var commentsAdapter: CommentsRecyclerAdapter
    private var commentLikeHandler = Handler(Looper.getMainLooper())

    private var pollId = ""
    private var pollLikeStatus = false
    private var likeBtnStatus = false

    private lateinit var loadingDialog: AlertDialog
    private lateinit var retryDialog: AlertDialog

    private val pollOptionsTvList = mutableListOf<TextView>()
    private val pollOptionsResultLayoutList = mutableListOf<LinearLayout>()
    private val pollOptionsVotesTvList = mutableListOf<TextView>()
    private val pollOptionsProgressList = mutableListOf<ProgressBar>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_poll_details)

        viewModel = ViewModelProvider(this)[PollDetailsViewModel::class.java]
        commentsViewModel = ViewModelProvider(this)[CommentsViewModel::class.java]

        //get poll id from intent
        pollId = intent.getStringExtra(Constants.POLL_ID)!!

        initDialogs(pollId)
        initPollOptionLayouts()
        binding.pollDetailsBackBtn.setOnClickListener { onBackPressed() }
        getPollDetails(pollId)
    }

    private fun initPollOptionLayouts() {
        pollOptionsTvList.addAll(
            listOf(
                binding.pollDetailsOption1Tv,
                binding.pollDetailsOption2Tv,
                binding.pollDetailsOption3Tv,
                binding.pollDetailsOption4Tv,
                binding.pollDetailsOption5Tv,
                binding.pollDetailsOption6Tv,
            )
        )

        pollOptionsResultLayoutList.addAll(
            listOf(
                binding.pollDetailsOption1Ll,
                binding.pollDetailsOption2Ll,
                binding.pollDetailsOption3Ll,
                binding.pollDetailsOption4Ll,
                binding.pollDetailsOption5Ll,
                binding.pollDetailsOption6Ll,
            )
        )

        pollOptionsVotesTvList.addAll(
            listOf(
                binding.pollDetailsOption1Votes,
                binding.pollDetailsOption2Votes,
                binding.pollDetailsOption3Votes,
                binding.pollDetailsOption4Votes,
                binding.pollDetailsOption5Votes,
                binding.pollDetailsOption6Votes,
            )
        )

        pollOptionsProgressList.addAll(
            listOf(
                binding.pollDetailsOption1Progress,
                binding.pollDetailsOption2Progress,
                binding.pollDetailsOption3Progress,
                binding.pollDetailsOption4Progress,
                binding.pollDetailsOption5Progress,
                binding.pollDetailsOption6Progress,
            )
        )
    }

    /**
     * METHOD TO INITIALIZE DIALOGS
     */
    private fun initDialogs(pollId: String) {
        val dialogBinding = LoadingDialogBinding.inflate(layoutInflater)
        loadingDialog = MaterialAlertDialogBuilder(this).setView(dialogBinding.root)
            .setCancelable(false).show()
        loadingDialog.dismiss()

        retryDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Oops!")
            .setMessage("Error getting profile")
            .setCancelable(false)
            .setPositiveButton("Retry") { dialog, _ ->
                dialog.dismiss()
                getPollDetails(pollId)
            }
            .setNegativeButton("Cancel") { _, _ ->
                setResult(Constants.RESULT_CLOSE)
                finish()
            }
            .show()
        retryDialog.dismiss()
    }

    private fun getPollDetails(pollId: String) {
        //check if poll is in poll list
        if (viewModel.isPollInAlreadyFetched(pollId)) {
            //if yes, get poll from poll repository
            viewModel.getPollFromPollRepository(pollId)
            setDetails()
        } else {
            //if not, get poll from server
        }
    }

    private fun setDetails() {
        val poll = viewModel.poll.value!!
        setUserInfo(poll)
        viewModel.isPollVoted.observe(this) {
            setPollData()
            if (it != null && it != Constants.API_SUCCESS)
                Toast.makeText(this, "Error voting", Toast.LENGTH_SHORT).show()
        }

        initLikeButton(poll)

    }

    private fun setUserInfo(poll: PollModel) {
        //set user Image
        Glide.with(this)
            .load(poll.userImage)
            .placeholder(R.drawable.ic_profile)
            .circleCrop()
            .into(binding.pollDetailsUserImage)

        //set user name and time
        binding.pollDetailsUsername.text = poll.username
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = dateFormat.parse(poll.createdAt)

        binding.pollDetailsTime.text = DateUtils.getRelativeTimeSpanString(
            date!!.time,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )
    }

    private fun setPollData() {
        //for fetching latest poll data
        val poll = viewModel.poll.value!!

        binding.pollDetailsTitle.apply {
            text = poll.title
            visibility = if (poll.title.isEmpty()) View.GONE else View.VISIBLE
        }
        binding.pollDetailsContent.apply {
            text = poll.content
            visibility = if (poll.content.isEmpty()) View.GONE else View.VISIBLE
        }


        //hiding loading progress bar and showing poll options
        binding.pollDetailsLottieLoading.visibility =
            if (poll.isVoting) View.VISIBLE else View.GONE
        binding.pollDetailsOptionsLl.foreground =
            if (poll.isVoting) ColorDrawable(Color.WHITE) else ColorDrawable(Color.TRANSPARENT)


        //setting the poll options
        val pollOptions = poll.pollOptions
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
                    if (poll.isVoted && pollOptions[i].votedBy.any { it.username == poll.username }) {
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
                    if (!poll.isVoted)
                        viewModel.pollVote(this@PollDetailsActivity, poll.pollId, pollOptions[i].id)
                }
            }

            //result layout visibility
            pollOptionsResultLayoutList[i].visibility =
                if (poll.isVoted) View.VISIBLE else View.GONE

            if (poll.isVoted) {
                //setting votes percentage
                pollOptionsVotesTvList[i].text =
                    String.format(
                        "%d%%",
                        (pollOptions[i].votes * 100) / poll.totalVotes
                    )

                //setting votes progress
                pollOptionsProgressList[i].apply {
                    max = maxVotes
                    progress = pollOptions[i].votes
                }
            }
        }

        //view results button
        binding.pollDetailsViewResultsBtn.apply {
            visibility =
                if (poll.pollOptions.any { it.votedBy.isNotEmpty() } && poll.isVoted) View.VISIBLE else View.GONE
            setOnClickListener {
                val intent = Intent(this@PollDetailsActivity, PollResultsActivity::class.java)
                intent.putExtra(Constants.POLL_ID, poll.pollId)
                startActivity(intent)
            }
        }
    }

    private fun initLikeButton(poll: PollModel) {
        //set poll like count
        binding.pollDetailsLikesCount.text = poll.likes.toString()
        //local variable for realtime like button change
        var pollIsLiked = poll.isLiked
        //setting like and comment button click listeners
        binding.pollDetailsLikeBtn.apply {
            setOnClickListener {
                //changing the like button icon every time it is clicked
                pollIsLiked = !pollIsLiked
                //poll like logic
                likePoll(poll.isLiked, pollIsLiked)

                setCompoundDrawablesWithIntrinsicBounds(
                    if (pollIsLiked) {
                        R.drawable.ic_like_filled
                    } else R.drawable.ic_like,
                    0,
                    0,
                    0
                )
                //changing the likes count every time the like button is clicked based on the current state of the poll
                binding.pollDetailsLikesCount.text =
                    if (pollIsLiked) {
                        binding.pollDetailsLikesCount.text.toString().toInt().plus(1).toString()
                    } else {
                        binding.pollDetailsLikesCount.text.toString().toInt().minus(1).toString()
                    }
            }
            //checking if the current user has liked the poll
            setCompoundDrawablesWithIntrinsicBounds(
                if (pollIsLiked) {
                    R.drawable.ic_like_filled
                } else R.drawable.ic_like,
                0,
                0,
                0
            )
        }
    }

    private fun likePoll(isLiked: Boolean, btnLikeStatus: Boolean) {
        pollLikeStatus = isLiked
        likeBtnStatus = btnLikeStatus
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (pollLikeStatus != likeBtnStatus) {
            viewModel.likePoll(this, pollId)
        }
        finish()
    }
}