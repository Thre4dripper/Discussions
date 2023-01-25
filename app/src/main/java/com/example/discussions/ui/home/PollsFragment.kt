package com.example.discussions.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.discussions.Constants
import com.example.discussions.adapters.PollsRecyclerAdapter
import com.example.discussions.adapters.interfaces.LikeCommentInterface
import com.example.discussions.adapters.interfaces.PollClickInterface
import com.example.discussions.databinding.FragmentPollsBinding
import com.example.discussions.ui.PollResultsActivity
import com.example.discussions.viewModels.HomeViewModel
import com.example.discussions.viewModels.UserPollsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PollsFragment : Fragment(), PollClickInterface, LikeCommentInterface {
    private val TAG = "PollsFragment"

    private lateinit var binding: FragmentPollsBinding
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var viewModel: UserPollsViewModel

    private lateinit var pollsAdapter: PollsRecyclerAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPollsBinding.inflate(inflater, container, false)
        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        viewModel = ViewModelProvider(requireActivity())[UserPollsViewModel::class.java]

        binding.pollsRv.apply {
            pollsAdapter = PollsRecyclerAdapter(this@PollsFragment, this@PollsFragment)
            adapter = pollsAdapter
        }

        binding.pollsSwipeLayout.setOnRefreshListener {
            homeViewModel.refreshAllUserPolls(
                requireContext()
            )
        }

        getAllUserPolls()
        return binding.root
    }

    private fun getAllUserPolls() {
        binding.pollsProgressBar.visibility = View.VISIBLE
        homeViewModel.userPollsList.observe(viewLifecycleOwner) {
            if (it != null) {
                pollsAdapter.submitList(it) {
                    if (HomeViewModel.postsOrPollsScrollToTop)
                        binding.pollsRv.scrollToPosition(0)
                }
                //hiding all loading
                binding.pollsSwipeLayout.isRefreshing = false
                binding.pollsProgressBar.visibility = View.GONE
                binding.pollsLottieNoData.visibility = View.GONE

                //when empty list is loaded
                if (it.isEmpty()) {
                    binding.pollsLottieNoData.visibility = View.VISIBLE
                    val error = homeViewModel.isUserPollsFetched.value

                    //when empty list is due to network error
                    if (error != Constants.API_SUCCESS) {
                        Toast.makeText(
                            requireContext(),
                            homeViewModel.isUserPollsFetched.value,
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

        homeViewModel.getAllUserPolls(requireContext())
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
        homeViewModel.isPollVoted.observe(this) {
            if (it != null) {
                if (it == Constants.API_FAILED)
                    Toast.makeText(requireContext(), "Problem Voting Poll", Toast.LENGTH_SHORT)
                        .show()
            }
        }

        homeViewModel.pollVote(requireContext(), pollId, optionId)
    }

    override fun onPollResult(pollId: String) {
        val intent = Intent(requireContext(), PollResultsActivity::class.java)
        intent.putExtra(Constants.POLL_ID, pollId)
        startActivity(intent)
    }

    override fun onLike(postOrPollId: String, isLiked: Boolean, btnLikeStatus: Boolean) {
        homeViewModel.isPollLikedChanged.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it == Constants.API_FAILED) {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                } else if (it == Constants.AUTH_FAILURE_ERROR) {
                    requireActivity().setResult(Constants.RESULT_LOGOUT)
                    requireActivity().finish()
                }
            }
        }
        homeViewModel.likePoll(requireContext(), postOrPollId)
    }

    override fun onComment(id: String, type: String) {

    }
}