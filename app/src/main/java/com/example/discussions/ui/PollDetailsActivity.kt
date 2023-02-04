package com.example.discussions.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_poll_details)

        viewModel = ViewModelProvider(this)[PollDetailsViewModel::class.java]
        commentsViewModel = ViewModelProvider(this)[CommentsViewModel::class.java]

        //get poll id from intent
        pollId = intent.getStringExtra(Constants.POLL_ID)!!

        initDialogs(pollId)
        binding.pollDetailsBackBtn.setOnClickListener { onBackPressed() }
        getPollDetails(pollId)
    }

    /**
     * METHOD TO INITIALIZE DIALOGS
     */
    private fun initDialogs(postId: String) {
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
                getPollDetails(postId)
            }
            .setNegativeButton("Cancel") { _, _ ->
                setResult(Constants.RESULT_CLOSE)
                finish()
            }
            .show()
        retryDialog.dismiss()
    }

    private fun getPollDetails(pollId: String) {
        //check if post is in post list
        if (viewModel.isPollInAlreadyFetched(pollId)) {
            //if yes, get post from post repository
            viewModel.getPollFromPollRepository(pollId)
            setDetails()
        } else {
            //if not, get post from server
        }
    }

    private fun setDetails() {
        val poll = viewModel.poll.value!!
        setUserInfo(poll)

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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (pollLikeStatus != likeBtnStatus) {
//            viewModel.likePoll(this, pollId)
        }
        finish()
    }
}