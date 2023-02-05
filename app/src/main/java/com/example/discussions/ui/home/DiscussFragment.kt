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
import com.example.discussions.adapters.interfaces.LikeCommentInterface
import com.example.discussions.adapters.interfaces.PostClickInterface
import com.example.discussions.databinding.FragmentDiscussBinding
import com.example.discussions.repositories.PostRepository
import com.example.discussions.ui.PostDetailsActivity
import com.example.discussions.ui.bottomSheets.comments.CommentsBS
import com.example.discussions.viewModels.HomeViewModel

class DiscussFragment : Fragment(), LikeCommentInterface, PostClickInterface {
    private val TAG = "DiscussFragment"

    private lateinit var binding: FragmentDiscussBinding
    private lateinit var homeViewModel: HomeViewModel

    private lateinit var discussAdapter: DiscussionsRecyclerAdapter
    private var handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDiscussBinding.inflate(inflater, container, false)
        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        binding.discussionRv.apply {
            discussAdapter = DiscussionsRecyclerAdapter(this@DiscussFragment, this@DiscussFragment)
            adapter = discussAdapter
        }

        binding.discussSwipeLayout.setOnRefreshListener {
            homeViewModel.refreshAllPosts(
                requireContext()
            )
        }
        getAllPosts()
        return binding.root
    }

    private fun getAllPosts() {
        binding.discussionProgressBar.visibility = View.VISIBLE
        homeViewModel.postsList.observe(viewLifecycleOwner) {
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
                    val error = homeViewModel.isPostsFetched.value

                    //when empty list is due to network error
                    if (error != Constants.API_SUCCESS) {
                        Toast.makeText(
                            requireContext(),
                            homeViewModel.isPostsFetched.value,
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

        homeViewModel.getAllPosts(requireContext())
    }

    override fun onLike(postOrPollId: String, isLiked: Boolean, btnLikeStatus: Boolean) {
        homeViewModel.isPostLikedChanged.observe(viewLifecycleOwner) {
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
            handler.removeCallbacksAndMessages(postOrPollId)
            handler.postDelayed({
                if (isLiked == btnLikeStatus)
                    homeViewModel.likePost(requireContext(), postOrPollId)

            }, postOrPollId, Constants.LIKE_DEBOUNCE_TIME)
        }
        //debouncing the like button below android P
        else {
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                if (isLiked == btnLikeStatus)
                    homeViewModel.likePost(requireContext(), postOrPollId)

            }, Constants.LIKE_DEBOUNCE_TIME)
        }
    }

    override fun onComment(id: String, type: String) {
        val count = PostRepository.allPostsList.value?.find { it.postId == id }?.comments ?: 0

        val commentsBS = CommentsBS(id, type, count)
        commentsBS.show(requireActivity().supportFragmentManager, commentsBS.tag)
    }

    override fun onPostClick(postId: String) {
        val intent = Intent(requireContext(), PostDetailsActivity::class.java)
        intent.putExtra(Constants.POST_ID, postId)
        startActivity(intent)
    }
}