package com.example.discussions.ui.comments

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.adapters.CommentsRecyclerAdapter
import com.example.discussions.adapters.interfaces.CommentInterface
import com.example.discussions.databinding.CommentsBsBinding
import com.example.discussions.models.CommentModel
import com.example.discussions.viewModels.CommentsViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CommentBottomSheet(
    private var parentContext: Context,
    var id: String,
    var type: String,
    private var commentCount: Int,
) : BottomSheetDialogFragment(), CommentInterface {

    private val TAG = "CommentBottomSheet"

    private lateinit var binding: CommentsBsBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private lateinit var viewModel: CommentsViewModel
    private lateinit var commentsAdapter: CommentsRecyclerAdapter

    private var commentType = type
    private var commentId: String? = null

    private var commentLikeHandler = Handler(Looper.getMainLooper())
    private var bsLikeHandler = Handler(Looper.getMainLooper())

    //variables for realtime update of like button in bs and parent post or poll
    private var bsParentLikeStatus = false
    private var bsLikeBtnStatus = false
    private var bsLikeTrigger: () -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = CommentsBsBinding.inflate(inflater, container, false)
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
        handleBottomSheetLike()

        setupCommentObservers()
        addCommentHandler()
    }

    private fun handleBottomSheetLike() {
        if (commentType == Constants.COMMENT_TYPE_POST) {
            initLikeButton(likeStatus = { viewModel.getPostLikeStatus(id) })

            //setting like trigger for bottom sheet
            bsLikeTrigger = {
                viewModel.likePost(
                    this@CommentBottomSheet.parentContext, this@CommentBottomSheet.id
                )
            }

        } else if (commentType == Constants.COMMENT_TYPE_POLL) {
            initLikeButton(likeStatus = { viewModel.getPollLikeStatus(id) })

            //setting like trigger for bottom sheet
            bsLikeTrigger = {
                viewModel.likePoll(
                    this@CommentBottomSheet.parentContext, this@CommentBottomSheet.id
                )
            }

        }
    }

    private fun initLikeButton(
        likeStatus: () -> Boolean
    ) {
        //getting like status of parent post or poll
        bsParentLikeStatus = likeStatus()
        bsLikeBtnStatus = bsParentLikeStatus
        binding.commentBsLikeBtn.apply {
            setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    if (bsLikeBtnStatus) R.drawable.ic_like_filled else R.drawable.ic_like,
                    null
                )
            )
            //checking if the current user has liked the post
            setOnClickListener {
                //switching like status of button on every click
                bsLikeBtnStatus = !bsLikeBtnStatus

                //getting latest like status of parent post or poll
                bsParentLikeStatus = likeStatus()

                //removing previous callbacks and adding new callback
                bsLikeHandler.removeCallbacksAndMessages(null)
                bsLikeHandler.postDelayed({
                    //like will only be triggered if the like status of parent post or poll is changed
                    if (bsParentLikeStatus != bsLikeBtnStatus) {
                        bsLikeTrigger()
                    }
                }, Constants.LIKE_DEBOUNCE_TIME)

                //setting like button drawable according to "bsLikeBtnStatus"
                setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        if (bsLikeBtnStatus) R.drawable.ic_like_filled else R.drawable.ic_like,
                        null
                    )
                )
            }
        }
    }

    /***
     * WHEN BS IS CLOSING LIKE STATUS SHOULD INSTANTLY BE UPDATED
     */
    override fun onDestroyView() {
        super.onDestroyView()
        bsLikeHandler.removeCallbacksAndMessages(null)
        if (bsParentLikeStatus != bsLikeBtnStatus) {
            bsLikeTrigger()
        }
    }

    private fun setupCommentObservers() {
        //setting add comment observer only once
        CommentControllers.addCommentObserver(
            requireContext(), viewModel, binding, viewLifecycleOwner
        ) { this.commentType = type }

        //setting edit comment observer only once
        CommentControllers.editCommentObserver(
            requireContext(), viewModel, binding, viewLifecycleOwner
        ) { this.commentType = type }

        //setting delete comment observer only once
        CommentControllers.deleteCommentObserver(
            requireContext(), viewModel, viewLifecycleOwner
        )

        //setting like comment observer only once
        CommentControllers.likeCommentObserver(
            requireContext(), viewModel, viewLifecycleOwner
        )
    }

    private fun getAllComments() {
        //resetting fetch comment type on refresh all comments
        commentType = type

        viewModel.commentsList.observe(viewLifecycleOwner) {
            if (it != null) {
                commentsAdapter.submitList(it) {
                    if (CommentsViewModel.commentsScrollToTop) binding.commentsRv.scrollToPosition(0)
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
                    if (error != Constants.API_SUCCESS && error != null) {
                        Toast.makeText(
                            requireContext(), error, Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (error == Constants.AUTH_FAILURE_ERROR) {
                        requireActivity().setResult(Constants.RESULT_LOGOUT)
                        requireActivity().finish()
                    }
                }
            }
        }

        if (commentType == Constants.COMMENT_TYPE_POST) viewModel.getComments(
            requireContext(), id, null
        )
        else if (commentType == Constants.COMMENT_TYPE_POLL) viewModel.getComments(
            requireContext(), null, id
        )
    }

    private fun addCommentHandler() {
        //preconfiguring add comment button
        binding.addCommentBtn.apply {
            isEnabled = false
            drawable.alpha = 100
            setOnClickListener {
                createEditComment(binding.addCommentEt.text.toString())
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

    private fun createEditComment(content: String) {
        binding.commentAddProgressBar.visibility = View.VISIBLE
        binding.addCommentBtn.visibility = View.GONE

        when (commentType) {
            Constants.COMMENT_TYPE_POST -> viewModel.createComment(
                requireContext(), postId = id, null, null, content
            )
            Constants.COMMENT_TYPE_POLL -> viewModel.createComment(
                requireContext(), null, pollId = id, null, content
            )
            Constants.COMMENT_TYPE_REPLY -> viewModel.createComment(
                requireContext(), null, null, commentId = commentId, content
            )
            Constants.COMMENT_TYPE_EDIT -> viewModel.editComment(
                requireContext(), commentId!!, content
            )
        }
    }

    override fun onCommentLikeChanged(commentId: String, isLiked: Boolean, btnLikeStatus: Boolean) {
        commentLikeHandler.removeCallbacksAndMessages(null)
        commentLikeHandler.postDelayed({
            if (isLiked == btnLikeStatus) {
                viewModel.likeComment(requireContext(), commentId)
            }
        }, Constants.LIKE_DEBOUNCE_TIME)
    }

    override fun onCommentDeleted(comment: CommentModel) {
        CommentControllers.commentDeleteHandler(requireContext(), viewModel, comment)
    }

    override fun onCommentReply(commentId: String, username: String) {
        this.commentId = commentId
        commentType = Constants.COMMENT_TYPE_REPLY

        binding.commentActionsCv.visibility = View.VISIBLE
        binding.commentActionTypeTv.text = getString(R.string.comment_action_label_reply)
        binding.commentActionContentTv.text = username
        binding.commentReplyCancelBtn.setOnClickListener {
            binding.commentActionsCv.visibility = View.GONE
            //restoring comment type
            commentType = type
        }
    }

    override fun onCommentEdit(commentId: String, content: String) {
        this.commentId = commentId
        commentType = Constants.COMMENT_TYPE_EDIT

        binding.commentActionsCv.visibility = View.VISIBLE
        binding.commentActionTypeTv.text = getString(R.string.comment_action_label_edit)
        binding.commentActionContentTv.text = content
        binding.commentReplyCancelBtn.setOnClickListener {
            binding.commentActionsCv.visibility = View.GONE
            //restoring comment type
            commentType = type
        }
    }

    override fun onCommentCopy(content: String) {
        CommentControllers.commentCopyHandler(requireContext(), content)
    }

    override fun onCommentLongClick(comment: CommentModel) {
        val optionsBottomSheet = OptionsBottomSheet(comment, this@CommentBottomSheet)
        optionsBottomSheet.show(requireActivity().supportFragmentManager, optionsBottomSheet.tag)
    }
}