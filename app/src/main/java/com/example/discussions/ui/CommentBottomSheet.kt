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
import com.example.discussions.adapters.interfaces.CommentInterface
import com.example.discussions.databinding.CommentBsLayoutBinding
import com.example.discussions.viewModels.CommentsViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CommentBottomSheet(
    var id: String,
    var type: String,
    private var commentCount: Int,
) :
    BottomSheetDialogFragment(), CommentInterface {

    private val TAG = "CommentBottomSheet"

    private lateinit var binding: CommentBsLayoutBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private lateinit var viewModel: CommentsViewModel
    private lateinit var commentsAdapter: CommentsRecyclerAdapter

    private var commentType = type
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
            commentsAdapter = CommentsRecyclerAdapter(this@CommentBottomSheet)
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

        if (commentType == Constants.COMMENT_TYPE_POST)
            viewModel.getComments(requireContext(), id, null)
        else
            viewModel.getComments(requireContext(), null, id)
    }

    private fun addCommentHandler() {
        //setting add comment observer only once
        viewModel.isCommentAdded.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it == Constants.API_SUCCESS) {
                    Toast.makeText(requireContext(), "Comment added", Toast.LENGTH_SHORT).show()
                    binding.commentReplyCv.visibility = View.GONE
                } else {
                    Toast.makeText(requireContext(), "Error Adding Comment", Toast.LENGTH_SHORT)
                        .show()
                }
                binding.commentAddProgressBar.visibility = View.GONE
                binding.addCommentBtn.visibility = View.VISIBLE
            }
        }

        //preconfiguring add comment button
        binding.addCommentBtn.apply {
            isEnabled = false
            drawable.alpha = 100
            setOnClickListener {
                createComment(binding.addCommentEt.text.toString())
                binding.addCommentEt.text.clear()
            }
        }

        //controlling add comment button based on text
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

    private fun createComment(content: String) {
        binding.commentAddProgressBar.visibility = View.VISIBLE
        binding.addCommentBtn.visibility = View.GONE

        when (commentType) {
            Constants.COMMENT_TYPE_POST -> viewModel.createComment(
                requireContext(),
                id,
                null,
                null,
                content
            )
            Constants.COMMENT_TYPE_POLL -> viewModel.createComment(
                requireContext(),
                null,
                id,
                null,
                content
            )
            Constants.COMMENT_TYPE_NESTED -> viewModel.createComment(
                requireContext(),
                null,
                null,
                commentId,
                content
            )
        }
    }

    override fun onCommentLikeChanged(commentId: String, isLiked: Boolean) {

    }

    override fun onCommentDeleted(commentId: String) {
    }

    override fun onCommentReply(commentId: String) {
        this.commentId = commentId
        commentType = Constants.COMMENT_TYPE_NESTED
        binding.commentReplyCv.visibility = View.VISIBLE
        binding.commentReplyUsernameTv.text = commentId
        binding.commentReplyCancelBtn.setOnClickListener {
            binding.commentReplyCv.visibility = View.GONE
            //restoring comment type
            commentType = type
        }
    }

    override fun onCommentEdit(commentId: String, content: String) {
    }
}