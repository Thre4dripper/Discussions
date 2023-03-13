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
import com.example.discussions.adapters.interfaces.PostClickInterface
import com.example.discussions.databinding.FragmentDiscussBinding
import com.example.discussions.models.PollModel
import com.example.discussions.models.PostModel
import com.example.discussions.repositories.DiscussionRepository
import com.example.discussions.ui.CreateEditPostActivity
import com.example.discussions.ui.PollDetailsActivity
import com.example.discussions.ui.PollResultsActivity
import com.example.discussions.ui.PostDetailsActivity
import com.example.discussions.ui.bottomSheets.DiscussionOptionsBS
import com.example.discussions.ui.bottomSheets.comments.CommentsBS
import com.example.discussions.viewModels.HomeViewModel
import com.example.discussions.viewModels.PostsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DiscussFragment : Fragment(), LikeCommentInterface, PostClickInterface, PollClickInterface,
    DiscussionMenuInterface {
    private val TAG = "DiscussFragment"

    private lateinit var binding: FragmentDiscussBinding
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var postsViewModel: PostsViewModel

    private lateinit var discussAdapter: DiscussionsRecyclerAdapter
    private var handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDiscussBinding.inflate(inflater, container, false)
        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        postsViewModel = ViewModelProvider(requireActivity())[PostsViewModel::class.java]

        binding.discussionRv.apply {
            discussAdapter = DiscussionsRecyclerAdapter(
                this@DiscussFragment,
                this@DiscussFragment,
                this@DiscussFragment,
                this@DiscussFragment,
            )
            adapter = discussAdapter
        }

        binding.discussSwipeLayout.setOnRefreshListener {
            homeViewModel.refreshAllDiscussions(
                requireContext()
            )
        }

        getAllDiscussions()
        return binding.root
    }

    private fun getAllDiscussions() {
        binding.discussionProgressBar.visibility = View.VISIBLE
        homeViewModel.discussions.observe(viewLifecycleOwner) {
            if (it != null) {
                discussAdapter.submitList(it) {
                    if (HomeViewModel.postsOrPollsOrNotificationsScrollToTop)
                        binding.discussionRv.scrollToPosition(0)
                }
                //hiding all loading
                binding.discussSwipeLayout.isRefreshing = false
                binding.discussionProgressBar.visibility = View.GONE
                binding.discussLottieNoData.visibility = View.GONE

                //when empty list is loaded
                if (it.isEmpty()) {
                    binding.discussLottieNoData.visibility = View.VISIBLE
                    val error = homeViewModel.isDiscussionsFetched.value

                    //when empty list is due to network error
                    if (error != Constants.API_SUCCESS) {
                        Toast.makeText(
                            requireContext(),
                            homeViewModel.isDiscussionsFetched.value,
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

        homeViewModel.getAllDiscussions(requireContext(), 1)
    }

    override fun onPostLike(postId: String, isLiked: Boolean, btnLikeStatus: Boolean) {
        postsViewModel.isPostLikedChanged.observe(viewLifecycleOwner) {
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
            handler.removeCallbacksAndMessages(postId)
            handler.postDelayed({
                if (isLiked == btnLikeStatus)
                    postsViewModel.likePost(requireContext(), postId)

            }, postId, Constants.LIKE_DEBOUNCE_TIME)
        }
        //debouncing the like button below android P
        else {
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                if (isLiked == btnLikeStatus)
                    postsViewModel.likePost(requireContext(), postId)

            }, Constants.LIKE_DEBOUNCE_TIME)
        }
    }

    override fun onPostComment(postId: String) {
        val count =
            DiscussionRepository.discussions.value?.find { it.post?.postId == postId }?.post?.comments
                ?: 0

        val commentsBS = CommentsBS(postId, Constants.COMMENT_TYPE_POST, count)
        commentsBS.show(requireActivity().supportFragmentManager, commentsBS.tag)
    }

    override fun onPollLike(pollId: String, isLiked: Boolean, btnLikeStatus: Boolean) {
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

        //debouncing the like button above android P
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            handler.removeCallbacksAndMessages(pollId)
            handler.postDelayed({
                if (isLiked == btnLikeStatus)
                    homeViewModel.likePoll(requireContext(), pollId)

            }, pollId, Constants.LIKE_DEBOUNCE_TIME)
        }
        //debouncing the like button below android P
        else {
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                if (isLiked == btnLikeStatus)
                    homeViewModel.likePoll(requireContext(), pollId)

            }, Constants.LIKE_DEBOUNCE_TIME)
        }
    }

    override fun onPollComment(pollId: String) {
        val count =
            DiscussionRepository.discussions.value?.find { it.poll?.pollId == pollId }?.poll?.comments
                ?: 0

        val commentsBS = CommentsBS(pollId, Constants.COMMENT_TYPE_POLL, count)
        commentsBS.show(requireActivity().supportFragmentManager, commentsBS.tag)
    }

    override fun onPostClick(postId: String) {
        val intent = Intent(requireContext(), PostDetailsActivity::class.java)
        intent.putExtra(Constants.POST_ID, postId)
        startActivity(intent)
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

    override fun onPollClick(pollId: String) {
        val intent = Intent(requireContext(), PollDetailsActivity::class.java)
        intent.putExtra(Constants.POLL_ID, pollId)
        startActivity(intent)
    }


    override fun onPostMenuClicked(post: PostModel) {
        val optionsBs = DiscussionOptionsBS(post, null, this@DiscussFragment)
        optionsBs.show(requireActivity().supportFragmentManager, optionsBs.tag)
    }

    override fun onPollMenuClicked(poll: PollModel) {
        val optionsBs = DiscussionOptionsBS(null, poll, this@DiscussFragment)
        optionsBs.show(requireActivity().supportFragmentManager, optionsBs.tag)
    }

    override fun onPostEdit(postId: String) {
        val intent = Intent(requireContext(), CreateEditPostActivity::class.java)
        intent.putExtra(Constants.POST_MODE, Constants.MODE_EDIT_POST)
        intent.putExtra(Constants.POST_ID, postId)
        val post = homeViewModel.discussions.value?.find { it.post!!.postId == postId }!!.post!!
        intent.putExtra(Constants.POST_TITLE, post.title)
        intent.putExtra(Constants.POST_CONTENT, post.content)
        intent.putExtra(Constants.POST_IMAGE, post.postImage)
        startActivity(intent)
    }

    override fun onPostDelete(postId: String) {
        MaterialAlertDialogBuilder(requireContext()).setTitle("Delete")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Confirm") { dialog, _ ->
                dialog.dismiss()
                deletePost(postId)
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    /**
     * METHOD FOR SENDING DELETE POST REQ TO THE VIEW MODEL
     */
    private fun deletePost(postId: String) {
        //post delete api observer
        postsViewModel.isPostDeleted.observe(this) {
            if (it != null) {
                if (it == Constants.API_SUCCESS) Toast.makeText(
                    requireContext(),
                    "Post Deleted",
                    Toast.LENGTH_SHORT
                ).show()
                else if (it == Constants.API_FAILED) Toast.makeText(
                    requireContext(),
                    "Problem Deleting Post",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        postsViewModel.deletePost(requireContext(), postId)
    }

    override fun onPollDelete(pollId: String) {
        MaterialAlertDialogBuilder(requireContext()).setTitle("Delete")
            .setMessage("Are you sure you want to delete this poll?")
            .setPositiveButton("Confirm") { dialog, _ ->
                dialog.dismiss()
                deletePoll(pollId)
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    /**
     * METHOD FOR SENDING DELETE POLL REQ TO THE VIEW MODEL
     */

    private fun deletePoll(pollId: String) {
        //post delete api observer
        homeViewModel.isPollDeleted.observe(this) {
            if (it != null) {
                if (it == Constants.API_SUCCESS)
                    Toast.makeText(requireContext(), "Poll Deleted", Toast.LENGTH_SHORT).show()
                else if (it == Constants.API_FAILED)
                    Toast.makeText(requireContext(), "Problem Deleting Poll", Toast.LENGTH_SHORT)
                        .show()
            }
        }
        homeViewModel.deletePoll(requireContext(), pollId)
    }
}