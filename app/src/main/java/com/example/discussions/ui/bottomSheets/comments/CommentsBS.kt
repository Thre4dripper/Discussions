package com.example.discussions.ui.bottomSheets.comments

import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.example.discussions.databinding.BsCommentsBinding
import com.example.discussions.models.CommentModel
import com.example.discussions.viewModels.CommentsViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CommentsBS(
    var id: String,
    var type: String,
    private var commentCount: Int,
) : BottomSheetDialogFragment(), CommentInterface {

    private val TAG = "CommentsBS"

    private lateinit var binding: BsCommentsBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private lateinit var viewModel: CommentsViewModel
    private lateinit var commentsAdapter: CommentsRecyclerAdapter

    private var commentLikeHandler = Handler(Looper.getMainLooper())

    //variables for realtime update of like button in bs and parent post or poll
    private var postOrPollLikeStatus = false
    private var bsLikeBtnStatus = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = BsCommentsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[CommentsViewModel::class.java]

        CommentControllers.commentType = type
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        binding.commentsCountTv.text = commentCount.toString()
        binding.commentsCl.layoutParams.height =
            Resources.getSystem().displayMetrics.heightPixels / 2 + 400

        handleBottomSheetLike()

        //setting up comments recycler view
        binding.commentsRv.apply {
            commentsAdapter = CommentsRecyclerAdapter(this@CommentsBS)
            adapter = commentsAdapter

            //this is to disable dragging of bottom sheet when recycler view is scrolled
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    bottomSheetBehavior.isDraggable = newState == RecyclerView.SCROLL_STATE_IDLE
                }
            })
        }

        //setting up swipe to refresh
        binding.commentsSwipeLayout.setOnRefreshListener { getAllComments() }
        //getting all comments
        getAllComments()

        //setting all the comment observers that will restore comment type every time new comment is added or edited
        CommentControllers.setupCommentObservers(
            requireContext(),
            viewModel,
            binding.commentActionsCv,
            binding.commentAddProgressBar,
            binding.commentAddBtn,
            viewLifecycleOwner,
        ) { CommentControllers.commentType = type }

        //setting comment add button click handlers
        CommentControllers.addCommentHandler(
            requireContext(),
            binding.commentAddProgressBar,
            binding.commentAddBtn,
            binding.addCommentEt,
            id,
            viewModel
        )
    }

    private fun handleBottomSheetLike() {
        if (CommentControllers.commentType == Constants.COMMENT_TYPE_POST) {
            postOrPollLikeStatus = viewModel.getPostLikeStatus(id)
        } else if (CommentControllers.commentType == Constants.COMMENT_TYPE_POLL) {
            postOrPollLikeStatus = viewModel.getPollLikeStatus(id)
        }

        bsLikeBtnStatus = postOrPollLikeStatus
        initLikeButton()
    }

    private fun initLikeButton() {

        binding.commentBsLikeBtn.apply {
            setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    if (postOrPollLikeStatus) R.drawable.ic_like_filled else R.drawable.ic_like,
                    null
                )
            )
            //checking if the current user has liked the post
            setOnClickListener {
                //switching like status of button on every click
                bsLikeBtnStatus = !bsLikeBtnStatus

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

        if (postOrPollLikeStatus != bsLikeBtnStatus) {
            if (CommentControllers.commentType == Constants.COMMENT_TYPE_POST) {
                viewModel.likePost(requireContext(), id)
            } else if (CommentControllers.commentType == Constants.COMMENT_TYPE_POLL) {
                viewModel.likePoll(requireContext(), id)
            }
        }

        commentLikeHandler.removeCallbacksAndMessages(null)
        if (CommentControllers.commentId != null) {
            viewModel.likeComment(requireContext(), CommentControllers.commentId!!)
            CommentControllers.commentId = null
        }
    }

    private fun getAllComments() {
        //resetting fetch comment type on refresh all comments
        CommentControllers.commentType = type

        binding.commentsProgressBar.visibility = View.VISIBLE
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

        if (CommentControllers.commentType == Constants.COMMENT_TYPE_POST) viewModel.getComments(
            requireContext(), id, null
        )
        else if (CommentControllers.commentType == Constants.COMMENT_TYPE_POLL) viewModel.getComments(
            requireContext(), null, id
        )
    }

    override fun onCommentLikeChanged(commentId: String, isLiked: Boolean, btnLikeStatus: Boolean) {
        CommentControllers.commentId = commentId
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            commentLikeHandler.removeCallbacksAndMessages(commentId)
            commentLikeHandler.postDelayed({
                if (isLiked == btnLikeStatus) {
                    viewModel.likeComment(requireContext(), commentId)
                }
            }, commentId, Constants.LIKE_DEBOUNCE_TIME)
        } else {
            commentLikeHandler.removeCallbacksAndMessages(null)
            commentLikeHandler.postDelayed({
                if (isLiked == btnLikeStatus) {
                    viewModel.likeComment(requireContext(), commentId)
                }
            }, Constants.LIKE_DEBOUNCE_TIME)
        }
    }

    override fun onCommentDeleted(comment: CommentModel) {
        CommentControllers.commentDeleteHandler(requireContext(), viewModel, comment)
    }

    override fun onCommentReply(commentId: String, username: String) {
        CommentControllers.commentId = commentId
        CommentControllers.commentType = Constants.COMMENT_TYPE_REPLY

        binding.commentActionsCv.visibility = View.VISIBLE
        binding.commentActionTypeTv.text = getString(R.string.comment_action_label_reply)
        binding.commentActionContentTv.text = username
        binding.commentReplyCancelBtn.setOnClickListener {
            binding.commentActionsCv.visibility = View.GONE
            //restoring comment type
            CommentControllers.commentType = type
        }
    }

    override fun onCommentEdit(commentId: String, content: String) {
        CommentControllers.commentId = commentId
        CommentControllers.commentType = Constants.COMMENT_TYPE_EDIT

        binding.commentActionsCv.visibility = View.VISIBLE
        binding.commentActionTypeTv.text = getString(R.string.comment_action_label_edit)
        binding.commentActionContentTv.text = content
        binding.commentReplyCancelBtn.setOnClickListener {
            binding.commentActionsCv.visibility = View.GONE
            //restoring comment type
            CommentControllers.commentType = type
        }
    }

    override fun onCommentCopy(content: String) {
        CommentControllers.commentCopyHandler(requireContext(), content)
    }

    override fun onCommentLongClick(comment: CommentModel) {
        val optionsBS = OptionsBS(comment, this@CommentsBS)
        optionsBS.show(requireActivity().supportFragmentManager, optionsBS.tag)
    }
}