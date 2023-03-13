package com.example.discussions.ui.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.discussions.Constants
import com.example.discussions.adapters.DiscussionsRecyclerAdapter
import com.example.discussions.adapters.interfaces.DiscussionMenuInterface
import com.example.discussions.adapters.interfaces.LikeCommentInterface
import com.example.discussions.adapters.interfaces.PollClickInterface
import com.example.discussions.databinding.FragmentPollsBinding
import com.example.discussions.models.PollModel
import com.example.discussions.repositories.PollRepository
import com.example.discussions.ui.PollDetailsActivity
import com.example.discussions.ui.PollResultsActivity
import com.example.discussions.ui.bottomSheets.DiscussionOptionsBS
import com.example.discussions.ui.bottomSheets.comments.CommentsBS
import com.example.discussions.viewModels.HomeViewModel
import com.example.discussions.viewModels.PollsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PollsFragment : Fragment(), PollClickInterface, LikeCommentInterface,
    DiscussionMenuInterface {
    private val TAG = "PollsFragment"

    private lateinit var binding: FragmentPollsBinding
    private lateinit var viewModel: PollsViewModel

    private lateinit var pollsAdapter: DiscussionsRecyclerAdapter
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPollsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[PollsViewModel::class.java]

        binding.pollsRv.apply {
            pollsAdapter = DiscussionsRecyclerAdapter(
                this@PollsFragment,
                null,
                this@PollsFragment,
                this@PollsFragment
            )
            adapter = pollsAdapter
        }

        binding.pollsSwipeLayout.setOnRefreshListener {
            viewModel.refreshAllUserPolls(
                requireContext()
            )
        }

        getAllUserPolls()
        return binding.root
    }

    private fun getAllUserPolls() {
        binding.pollsProgressBar.visibility = View.VISIBLE
        viewModel.userPollsList.observe(viewLifecycleOwner) {
            if (it != null) {
                pollsAdapter.submitList(it) {
                    if (HomeViewModel.postsOrPollsOrNotificationsScrollToTop)
                        binding.pollsRv.scrollToPosition(0)
                }
                //hiding all loading
                binding.pollsSwipeLayout.isRefreshing = false
                binding.pollsProgressBar.visibility = View.GONE
                binding.pollsLottieNoData.visibility = View.GONE

                //when empty list is loaded
                if (it.isEmpty()) {
                    binding.pollsLottieNoData.visibility = View.VISIBLE
                    val error = viewModel.isUserPollsFetched.value

                    //when empty list is due to network error
                    if (error != Constants.API_SUCCESS) {
                        Toast.makeText(
                            requireContext(),
                            viewModel.isUserPollsFetched.value,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    if (error == Constants.AUTH_FAILURE_ERROR) {
                        requireActivity().setResult(Constants.RESULT_LOGOUT)
                        requireActivity().finish()
                    }
                }
            }
        }

        viewModel.getAllUserPolls(requireContext())
    }

    /**
     * METHOD FOR SENDING DELETE POST REQ TO THE VIEW MODEL
     */
    private fun deletePoll(pollId: String) {
        //post delete api observer
        viewModel.isPollDeleted.observe(this) {
            if (it != null) {
                if (it == Constants.API_SUCCESS)
                    Toast.makeText(requireContext(), "Poll Deleted", Toast.LENGTH_SHORT).show()
                else if (it == Constants.API_FAILED)
                    Toast.makeText(requireContext(), "Problem Deleting Poll", Toast.LENGTH_SHORT)
                        .show()
            }
        }
        viewModel.deletePoll(requireContext(), pollId)
    }

    override fun onPollVote(pollId: String, optionId: String) {
        viewModel.isPollVoted.observe(this) {
            if (it != null) {
                if (it == Constants.API_FAILED)
                    Toast.makeText(requireContext(), "Problem Voting Poll", Toast.LENGTH_SHORT)
                        .show()
            }
        }

        viewModel.pollVote(requireContext(), pollId, optionId)
    }

    override fun onPollResult(pollId: String) {
        val intent = Intent(requireContext(), PollResultsActivity::class.java)
        intent.putExtra(Constants.POLL_ID, pollId)
        startActivity(intent)
    }

    override fun onPollClick(pollId: String) {
        val intent = Intent(requireContext(), PollDetailsActivity::class.java)
        intent.putExtra(Constants.POLL_ID, pollId)
        startActivity(intent)
    }

    override fun onPollLike(pollId: String, isLiked: Boolean, btnLikeStatus: Boolean) {
        viewModel.isPollLikedChanged.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it == Constants.API_FAILED) {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                } else if (it == Constants.AUTH_FAILURE_ERROR) {
                    requireActivity().setResult(Constants.RESULT_LOGOUT)
                    requireActivity().finish()
                }
            }
        }

        //debouncing the like button above android P
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            handler.removeCallbacksAndMessages(pollId)
            handler.postDelayed({
                if (isLiked == btnLikeStatus)
                    viewModel.likePoll(requireContext(), pollId)

            }, pollId, Constants.LIKE_DEBOUNCE_TIME)
        }
        //debouncing the like button below android P
        else {
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                if (isLiked == btnLikeStatus)
                    viewModel.likePoll(requireContext(), pollId)

            }, Constants.LIKE_DEBOUNCE_TIME)
        }
    }

    override fun onPollComment(pollId: String) {
        val count =
            PollRepository.userPollsList.value?.find { it.poll!!.pollId == pollId }?.poll?.comments
                ?: 0

        val commentsBS = CommentsBS(pollId, Constants.COMMENT_TYPE_POLL, count)
        commentsBS.show(requireActivity().supportFragmentManager, commentsBS.tag)
    }

    override fun onPollMenuClicked(poll: PollModel) {
        val optionsBS = DiscussionOptionsBS(null, poll, this@PollsFragment)
        optionsBS.show(requireActivity().supportFragmentManager, optionsBS.tag)
    }

    override fun onPollDelete(pollId: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete")
            .setMessage("Are you sure you want to delete this poll?")
            .setPositiveButton("Confirm") { dialog, _ ->
                dialog.dismiss()
                deletePoll(pollId)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}