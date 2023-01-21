package com.example.discussions.ui

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.discussions.Constants
import com.example.discussions.adapters.CommentsRecyclerAdapter
import com.example.discussions.databinding.CommentBsLayoutBinding
import com.example.discussions.viewModels.CommentsViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CommentBottomSheet : BottomSheetDialogFragment() {

    private val TAG = "CommentBottomSheet"

    private lateinit var binding: CommentBsLayoutBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private lateinit var viewModel: CommentsViewModel
    private lateinit var commentsAdapter: CommentsRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = CommentBsLayoutBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[CommentsViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        binding.commentsRv.apply {
            commentsAdapter = CommentsRecyclerAdapter()
            adapter = commentsAdapter

            //this is to disable dragging of bottom sheet when recycler view is scrolled
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    bottomSheetBehavior.isDraggable = newState == RecyclerView.SCROLL_STATE_IDLE
                }
            })
        }

        binding.commentsCl.layoutParams.height =
            Resources.getSystem().displayMetrics.heightPixels - 200

        binding.commentsSwipeLayout.setOnRefreshListener {
            viewModel.refreshComments(requireContext(), 21, null)
        }
        getAllComments()
    }

    private fun getAllComments() {
        binding.commentsProgressBar.visibility = View.VISIBLE
        viewModel.commentsList.observe(viewLifecycleOwner) {
            if (it != null) {
                commentsAdapter.submitList(it) {
//                    if (viewModel.postsOrPollsScrollToTop)
//                        binding.commentsRv.scrollToPosition(0)
                }
                //hiding all loading
                binding.commentsSwipeLayout.isRefreshing = false
                binding.commentsProgressBar.visibility = View.GONE
//                binding.discussLottieNoData.visibility = View.GONE

                Log.d(TAG, "getAllComments: $it")

                //when empty list is loaded
                if (it.isEmpty()) {
//                    binding.discussLottieNoData.visibility = View.VISIBLE
                    val error = viewModel.isCommentsFetched.value

                    //when empty list is due to network error
                    if (error != Constants.API_SUCCESS) {
                        Toast.makeText(
                            requireContext(), viewModel.isCommentsFetched.value, Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (error == Constants.AUTH_FAILURE_ERROR) {
                        requireActivity().setResult(Constants.RESULT_LOGOUT)
                        requireActivity().finish()
                    }
                }
            }
        }

        viewModel.getComments(requireContext(), 21, null)
    }
}