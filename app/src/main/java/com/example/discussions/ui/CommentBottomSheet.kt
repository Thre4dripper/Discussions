package com.example.discussions.ui

import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.discussions.Constants
import com.example.discussions.adapters.CommentsRecyclerAdapter
import com.example.discussions.databinding.CommentBsLayoutBinding
import com.example.discussions.viewModels.CommentsViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CommentBottomSheet(
    var id: String,
    var type: String,
    private var commentCount: Int,
) :
    BottomSheetDialogFragment() {

    private val TAG = "CommentBottomSheet"

    private lateinit var binding: CommentBsLayoutBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private lateinit var viewModel: CommentsViewModel
    private lateinit var commentsAdapter: CommentsRecyclerAdapter

    private var commentId: String? = null

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

        binding.commentsCountTv.text = commentCount.toString()
        binding.commentsCl.layoutParams.height =
            Resources.getSystem().displayMetrics.heightPixels / 2 + 400

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

        binding.commentsSwipeLayout.setOnRefreshListener { getAllComments() }
        binding.commentsProgressBar.visibility = View.VISIBLE
        getAllComments()

        addCommentHandler()
    }

    private fun getAllComments() {
        viewModel.commentsList.observe(viewLifecycleOwner) {
            if (it != null) {
                commentsAdapter.submitList(it) {
                    if (CommentsViewModel.commentsScrollToTop)
                        binding.commentsRv.scrollToPosition(0)
                }
                //hiding all loading
                binding.commentsSwipeLayout.isRefreshing = false
                binding.commentsProgressBar.visibility = View.GONE
                binding.itemCommentLottie.visibility = View.GONE

                //when empty list is loaded
                if (it.isEmpty()) {
                    binding.itemCommentLottie.visibility = View.VISIBLE
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

        if (type == Constants.COMMENT_TYPE_POST)
            viewModel.getComments(requireContext(), id, null)
        else
            viewModel.getComments(requireContext(), null, id)
    }

    private fun addCommentHandler() {
        binding.addCommentBtn.apply {
            isEnabled = false
            drawable.alpha = 100
            setOnClickListener(onAddCommentListener)
        }

        binding.addCommentEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    binding.addCommentBtn.apply {
                        isEnabled = false
                        drawable.alpha = 100
                    }
                } else {
                    binding.addCommentBtn.apply {
                        isEnabled = true
                        drawable.alpha = 255
                    }
                }
            }

        })
    }

    private var onAddCommentListener = View.OnClickListener {
        createComment(binding.addCommentEt.text.toString())
        binding.addCommentEt.text.clear()
    }

    private fun createComment(content: String) {
        if (type == Constants.COMMENT_TYPE_POST)
            viewModel.createComment(
                requireContext(),
                id,
                null,
                null,
                content
            )
        else if (type == Constants.COMMENT_TYPE_POLL)
            viewModel.createComment(requireContext(), null, id, null, content)
    }
}