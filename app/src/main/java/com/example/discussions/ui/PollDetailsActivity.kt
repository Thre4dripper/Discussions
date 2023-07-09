package com.example.discussions.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateUtils
import android.util.Log
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
import com.example.discussions.MyApplication
import com.example.discussions.R
import com.example.discussions.adapters.CommentsRecyclerAdapter
import com.example.discussions.adapters.DiscussionsRecyclerAdapter
import com.example.discussions.adapters.interfaces.CommentInterface
import com.example.discussions.adapters.interfaces.DiscussionMenuInterface
import com.example.discussions.databinding.ActivityPollDetailsBinding
import com.example.discussions.databinding.LoadingDialogBinding
import com.example.discussions.models.CommentModel
import com.example.discussions.models.PollModel
import com.example.discussions.models.PostModel
import com.example.discussions.ui.bottomSheets.DiscussionOptionsBS
import com.example.discussions.ui.bottomSheets.comments.CommentControllers
import com.example.discussions.ui.bottomSheets.comments.OptionsBS
import com.example.discussions.viewModels.CommentsViewModel
import com.example.discussions.viewModels.PollDetailsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class PollDetailsActivity : AppCompatActivity(), CommentInterface, DiscussionMenuInterface {
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
        binding.pollDetailsBackBtn.setOnClickListener {
            @Suppress("DEPRECATION")
            onBackPressed()
        }

        if (MyApplication.isUsernameInitialized())
            getPollDetails(pollId)
        else {
            initUsername()
        }
    }

    private fun initUsername() {
        loadingDialog.show()
        viewModel.isUsernameFetched.observe(this) {
            if (it != null) {
                loadingDialog.dismiss()
                if (it == Constants.API_SUCCESS) {
                    getPollDetails(pollId)
                } else {
                    //retry dialog configured for retrying to get username
                    retryDialog = "Oops".initRetryDialog("Error getting user details", {
                        //positive button
                        retryDialog.dismiss()
                        loadingDialog.show()
                        viewModel.getUsername(this)
                    }, {
                        //negative button
                        setResult(Constants.RESULT_CLOSE)
                        finish()
                    })
                }
            }
        }
        viewModel.getUsername(this)
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

        //retry dialog configured for retrying to get poll details
        retryDialog = "Oops".initRetryDialog("Error getting poll details", {
            retryDialog.dismiss()
            loadingDialog.show()
            viewModel.getPollFromApi(this, pollId)
        }, {
            setResult(Constants.RESULT_CLOSE)
            finish()
        })
        retryDialog.dismiss()
    }

    /**
     * RETRY DIALOG BUILDING FUNCTION
     */
    private fun String.initRetryDialog(
        message: String,
        positiveFn: () -> Unit,
        negativeFn: () -> Unit
    ): AlertDialog {
        return MaterialAlertDialogBuilder(this@PollDetailsActivity)
            .setTitle(this)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Retry") { _, _ ->
                positiveFn()
            }
            .setNegativeButton("Cancel") { _, _ ->
                negativeFn()
            }
            .show()
    }

    private fun getPollDetails(pollId: String) {
        //check if poll is in poll list
        if (viewModel.isPollInAlreadyFetched(pollId)) {
            //if yes, get poll from poll repository
            viewModel.getPollFromRepository(pollId)
            setDetails()
        } else {
            //if not, get post from server
            loadingDialog.show()
            binding.pollDetailsSwipeRefresh.visibility = View.GONE
            viewModel.isPollFetched.observe(this) {
                if (it != null) {
                    loadingDialog.dismiss()
                    binding.pollDetailsSwipeRefresh.visibility = View.VISIBLE

                    if (it == Constants.API_SUCCESS) {
                        setDetails()
                    } else {
                        retryDialog.show()
                    }
                }
            }

            viewModel.getPollFromApi(this, pollId)
        }
    }

    private fun setDetails() {
        val poll = viewModel.poll.value!!
        setUserInfo(poll)

        //set poll data by observer, because of poll vote functionality, this is called after vote
        viewModel.isPollVoted.observe(this) {
            setPollData()
            if (it != null && it != Constants.API_SUCCESS)
                Toast.makeText(this, "Error voting", Toast.LENGTH_SHORT).show()
        }

        initLikeButton(poll)
        initMenuButton(poll)

        /*Comments Logic Starts Here*/
        //set comments recycler view
        binding.pollDetailsCommentsRv.apply {
            commentsAdapter = CommentsRecyclerAdapter(this@PollDetailsActivity)
            adapter = commentsAdapter
        }

        //set swipe refresh
        binding.pollDetailsSwipeRefresh.setOnRefreshListener { getComments(poll) }
        //get comments
        getComments(poll)

        //setting all the comment observers that will restore comment type every time new comment is added or edited
        CommentControllers.setupCommentObservers(
            this,
            commentsViewModel,
            binding.pollDetailsCommentActionsCv,
            binding.pollDetailsCommentAddProgressBar,
            binding.pollDetailsCommentAddBtn,
            this@PollDetailsActivity
        ) { CommentControllers.commentType = Constants.COMMENT_TYPE_POLL }

        //setting comment add button click handlers
        CommentControllers.addCommentHandler(
            this,
            binding.pollDetailsCommentAddProgressBar,
            binding.pollDetailsCommentAddBtn,
            binding.pollDetailsAddCommentEt,
            pollId,
            commentsViewModel
        )
    }

    private fun setUserInfo(poll: PollModel) {
        //set user Image
        Glide.with(this)
            .load(poll.userImage)
            .placeholder(R.drawable.ic_profile)
            .circleCrop()
            .into(binding.pollDetailsUserImage)

        //set user name and time
        binding.pollDetailsUsername.text =
            getString(R.string.username_display, poll.username)
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

        binding.pollDetailsPrivacyIcon.apply {
            visibility =
                if (poll.isPrivate && poll.username != MyApplication.username) View.VISIBLE else View.GONE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tooltipText = "Private poll, only creator can see the stats"
            }
            setOnClickListener {
                binding.pollDetailsPrivacyIcon.performLongClick()
            }
        }
        //view results button
        binding.pollDetailsViewResultsBtn.apply {
            visibility = if (poll.isVoted) {
                if (poll.isPrivate && poll.username != MyApplication.username)
                    View.GONE
                else View.VISIBLE
            } else View.GONE
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

    private fun initMenuButton(poll: PollModel) {
        binding.pollDetailsMenuOptions.setOnClickListener {
            onMenuClicked(null, poll, DiscussionsRecyclerAdapter.DISCUSSION_TYPE_POLL)
        }
    }

    private fun likePoll(isLiked: Boolean, btnLikeStatus: Boolean) {
        pollLikeStatus = isLiked
        likeBtnStatus = btnLikeStatus
    }

    private fun getComments(poll: PollModel) {
        //resetting fetch comment type on refresh all comments
        CommentControllers.commentType = Constants.COMMENT_TYPE_POLL

        binding.pollDetailsCommentsPb.visibility = View.VISIBLE
        binding.pollDetailsCommentsRv.visibility = View.GONE
        commentsViewModel.commentsList.observe(this) {
            if (it != null) {
                commentsAdapter.submitList(it) {
                    if (CommentsViewModel.commentsScrollToTop)
                        binding.pollDetailsCommentsRv.scrollToPosition(0)
                }
                //hiding all loading
                binding.pollDetailsSwipeRefresh.isRefreshing = false
                binding.pollDetailsCommentsPb.visibility = View.GONE
                binding.pollDetailsCommentsLottie.visibility = View.GONE
                binding.pollDetailsCommentsRv.visibility = View.VISIBLE

                Log.d(TAG, "getComments: $it")
                //when empty list is loaded
                if (it.isEmpty()) {
                    binding.pollDetailsCommentsLottie.visibility = View.VISIBLE
                    binding.pollDetailsCommentsRv.visibility = View.GONE

                    val error = commentsViewModel.isCommentsFetched.value

                    //when empty list is due to network error
                    if (error != Constants.API_SUCCESS && error != null) {
                        Toast.makeText(
                            this, error, Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (error == Constants.AUTH_FAILURE_ERROR) {
                        setResult(Constants.RESULT_LOGOUT)
                        finish()
                    }
                }
            }
        }

        commentsViewModel.getComments(this, null, poll.pollId)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (pollLikeStatus != likeBtnStatus) {
            viewModel.likePoll(this, pollId)
        }
        finish()
    }

    override fun onCommentLikeChanged(commentId: String, isLiked: Boolean, btnLikeStatus: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            commentLikeHandler.removeCallbacksAndMessages(commentId)
            commentLikeHandler.postDelayed({
                if (isLiked == btnLikeStatus) {
                    commentsViewModel.likeComment(this, commentId)
                }
            }, commentId, Constants.LIKE_DEBOUNCE_TIME)
        } else {
            commentLikeHandler.removeCallbacksAndMessages(null)
            commentLikeHandler.postDelayed({
                if (isLiked == btnLikeStatus) {
                    commentsViewModel.likeComment(this, commentId)
                }
            }, Constants.LIKE_DEBOUNCE_TIME)
        }
    }

    override fun onCommentDeleted(comment: CommentModel) {
        CommentControllers.commentDeleteHandler(this, commentsViewModel, comment)
    }

    override fun onCommentReply(commentId: String, username: String) {
        CommentControllers.commentId = commentId
        CommentControllers.commentType = Constants.COMMENT_TYPE_REPLY

        binding.pollDetailsCommentActionsCv.visibility = View.VISIBLE
        binding.pollDetailsCommentActionTypeTv.text = getString(R.string.comment_action_label_reply)
        binding.pollDetailsCommentActionContentTv.text = username
        binding.pollDetailsCommentReplyCancelBtn.setOnClickListener {
            binding.pollDetailsCommentActionsCv.visibility = View.GONE
            //restoring comment type
            CommentControllers.commentType = Constants.COMMENT_TYPE_POLL
        }
    }

    override fun onCommentEdit(commentId: String, content: String) {
        CommentControllers.commentId = commentId
        CommentControllers.commentType = Constants.COMMENT_TYPE_EDIT

        binding.pollDetailsCommentActionsCv.visibility = View.VISIBLE
        binding.pollDetailsCommentActionTypeTv.text = getString(R.string.comment_action_label_edit)
        binding.pollDetailsCommentActionContentTv.text = content
        binding.pollDetailsCommentReplyCancelBtn.setOnClickListener {
            binding.pollDetailsCommentActionsCv.visibility = View.GONE
            //restoring comment type
            CommentControllers.commentType = Constants.COMMENT_TYPE_POLL
        }
    }

    override fun onCommentCopy(content: String) {
        CommentControllers.commentCopyHandler(this, content)
    }

    override fun onCommentLongClick(comment: CommentModel) {
        val optionsBS = OptionsBS(comment, this@PollDetailsActivity)
        optionsBS.show(supportFragmentManager, optionsBS.tag)
    }

    override fun onMenuClicked(post: PostModel?, poll: PollModel?, type: Int) {
        /*IN USER POLLS ACTIVITY, THE POLL IS NOT NULL AND THE TYPE IS ALWAYS POLL*/

        val optionsBS = DiscussionOptionsBS(null, poll, this)
        optionsBS.show(supportFragmentManager, optionsBS.tag)
    }

    override fun onMenuEdit(postId: String?, pollId: String?, type: Int) {
        //Edit not allowed for polls
    }

    override fun onMenuDelete(postId: String?, pollId: String?, type: Int) {
        /*IN USER POLLS ACTIVITY, THE POLL ID IS NOT NULL AND THE TYPE IS ALWAYS POLL*/

        MaterialAlertDialogBuilder(this)
            .setTitle("Delete")
            .setMessage("Are you sure you want to delete this poll?")
            .setPositiveButton("Confirm") { dialog, _ ->
                dialog.dismiss()
                deletePoll(pollId!!)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * METHOD FOR SENDING DELETE POST REQ TO THE VIEW MODEL
     */
    private fun deletePoll(pollId: String) {
        //post delete api observer
        viewModel.isPollDeleted.observe(this) {
            if (it != null) {
                if (it == Constants.API_SUCCESS)
                    Toast.makeText(this, "Poll Deleted", Toast.LENGTH_SHORT).show()
                else if (it == Constants.API_FAILED)
                    Toast.makeText(this, "Problem Deleting Poll", Toast.LENGTH_SHORT)
                        .show()
            }
        }
        viewModel.deletePoll(this, pollId)
    }
}