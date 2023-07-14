package com.example.discussions.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.adapters.CommentsRecyclerAdapter
import com.example.discussions.adapters.DiscussionsRecyclerAdapter
import com.example.discussions.adapters.interfaces.CommentInterface
import com.example.discussions.adapters.interfaces.DiscussionMenuInterface
import com.example.discussions.databinding.ActivityPostDetailsBinding
import com.example.discussions.databinding.LoadingDialogBinding
import com.example.discussions.models.CommentModel
import com.example.discussions.models.PollModel
import com.example.discussions.models.PostModel
import com.example.discussions.ui.bottomSheets.DiscussionOptionsBS
import com.example.discussions.ui.bottomSheets.comments.CommentControllers
import com.example.discussions.ui.bottomSheets.comments.OptionsBS
import com.example.discussions.ui.home.HomeActivity
import com.example.discussions.viewModels.CommentsViewModel
import com.example.discussions.viewModels.PostDetailsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class PostDetailsActivity : AppCompatActivity(), CommentInterface, DiscussionMenuInterface {
    private val TAG = "PostDetailsActivity"

    private lateinit var binding: ActivityPostDetailsBinding
    private lateinit var viewModel: PostDetailsViewModel
    private lateinit var commentsViewModel: CommentsViewModel

    private lateinit var commentsAdapter: CommentsRecyclerAdapter
    private var commentLikeHandler = Handler(Looper.getMainLooper())

    private var postId = ""
    private var postLikeStatus = false
    private var likeBtnStatus = false

    private lateinit var loadingDialog: AlertDialog
    private lateinit var retryDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_post_details)

        viewModel = ViewModelProvider(this)[PostDetailsViewModel::class.java]
        commentsViewModel = ViewModelProvider(this)[CommentsViewModel::class.java]

        //get post id from intent
        postId = intent.getStringExtra(Constants.POST_ID)!!

        initDialogs(postId)
        binding.postDetailsBackBtn.setOnClickListener {
            @Suppress("DEPRECATION")
            onBackPressed()
        }
        getPostDetails(postId)
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
            .setMessage("Error getting post details.")
            .setCancelable(false)
            .setPositiveButton("Retry") { dialog, _ ->
                dialog.dismiss()
                loadingDialog.show()
                viewModel.getPostFromApi(this, postId)
            }
            .setNegativeButton("Cancel") { _, _ ->
                setResult(Constants.RESULT_CLOSE)
                finish()
            }
            .show()
        retryDialog.dismiss()
    }

    /**
     * METHOD TO GET POST DETAILS
     */
    private fun getPostDetails(postId: String) {
        //check if post is in post list
        if (viewModel.isPostInAlreadyFetched(postId)) {
            //if yes, get post from post repository
            viewModel.getPostFromRepository(postId)
            setDetails()
        } else {
            //if not, get post from server
            loadingDialog.show()
            binding.postDetailsSwipeRefresh.visibility = View.GONE
            viewModel.isPostFetched.observe(this) {
                if (it != null) {
                    loadingDialog.dismiss()
                    binding.postDetailsSwipeRefresh.visibility = View.VISIBLE

                    if (it == Constants.API_SUCCESS) {
                        setDetails()
                    } else {
                        retryDialog.show()
                    }
                }
            }

            viewModel.getPostFromApi(this, postId)
        }
    }

    private fun setDetails() {
        val post = viewModel.post.value!!
        setUserInfo(post)
        setPostData(post)
        initLikeButton(post)
        initMenuButton(post)

        /*Comments Logic Starts Here*/
        //set comments recycler view
        binding.postDetailsCommentsRv.apply {
            commentsAdapter = CommentsRecyclerAdapter(this@PostDetailsActivity)
            adapter = commentsAdapter
        }

        //set swipe refresh
        binding.postDetailsSwipeRefresh.setOnRefreshListener {
            commentsViewModel.refreshAllComments()
            commentsViewModel.getComments(this, post.postId, null)
        }

        //get comments
        paginatedFlow()
        commentsViewModel.refreshAllComments()
        getComments(post)

        //setting all the comment observers that will restore comment type every time new comment is added or edited
        CommentControllers.setupCommentObservers(
            this,
            commentsViewModel,
            binding.postDetailsCommentActionsCv,
            binding.postDetailsCommentAddProgressBar,
            binding.postDetailsCommentAddBtn,
            this@PostDetailsActivity
        ) { CommentControllers.commentType = Constants.COMMENT_TYPE_POST }

        //setting comment add button click handlers
        CommentControllers.addCommentHandler(
            this,
            binding.postDetailsCommentAddProgressBar,
            binding.postDetailsCommentAddBtn,
            binding.postDetailsAddCommentEt,
            postId,
            commentsViewModel
        )
    }

    private fun setUserInfo(post: PostModel) {
        //set user Image
        Glide.with(this)
            .load(post.userImage)
            .placeholder(R.drawable.ic_profile)
            .circleCrop()
            .into(binding.postDetailsUserImage)

        //set user name and time
        binding.postDetailsUsername.text =
            getString(R.string.username_display, post.username)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = dateFormat.parse(post.createdAt)

        binding.postDetailsTime.text = DateUtils.getRelativeTimeSpanString(
            date!!.time,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )

        //open profile on user image or name click
        binding.postDetailsUserImage.setOnClickListener {
            openProfile(post.username)
        }

        binding.postDetailsUsername.setOnClickListener {
            openProfile(post.username)
        }
    }

    private fun openProfile(username: String) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra(Constants.USERNAME, username)
        startActivity(intent)
    }

    private fun setPostData(post: PostModel) {
        //set post title and content
        binding.postDetailsTitle.apply {
            text = post.title
            visibility = if (post.title.isEmpty()) View.GONE else View.VISIBLE
        }
        binding.postDetailsContent.apply {
            text = post.content
            visibility = if (post.content.isEmpty()) View.GONE else View.VISIBLE
        }

        //set post image
        val image = post.postImage
        if (image != "") {
            binding.postDetailsImage.visibility = View.VISIBLE
            Glide.with(this)
                .load(image)
                .override(Target.SIZE_ORIGINAL)
                .into(binding.postDetailsImage)
            binding.postDetailsImage.setOnClickListener {
                val context = binding.postDetailsImage.context
                val intent = Intent(context, ZoomImageActivity::class.java)
                intent.putExtra(Constants.ZOOM_IMAGE_URL, image)
                context.startActivity(intent)
            }
        } else {
            binding.postDetailsImage.visibility = View.GONE
        }
    }

    private fun initLikeButton(post: PostModel) {
        //set post like count
        binding.postDetailsLikesCount.text = post.likes.toString()
        //local variable for realtime like button change
        var postIsLiked = post.isLiked
        //setting like and comment button click listeners
        binding.postDetailsLikeBtn.apply {
            setOnClickListener {
                //changing the like button icon every time it is clicked
                postIsLiked = !postIsLiked
                //post like logic
                likePost(post.isLiked, postIsLiked)

                setCompoundDrawablesWithIntrinsicBounds(
                    if (postIsLiked) {
                        R.drawable.ic_like_filled
                    } else R.drawable.ic_like,
                    0,
                    0,
                    0
                )
                //changing the likes count every time the like button is clicked based on the current state of the post
                binding.postDetailsLikesCount.text =
                    if (postIsLiked) {
                        binding.postDetailsLikesCount.text.toString().toInt().plus(1).toString()
                    } else {
                        binding.postDetailsLikesCount.text.toString().toInt().minus(1).toString()
                    }
            }
            //checking if the current user has liked the post
            setCompoundDrawablesWithIntrinsicBounds(
                if (postIsLiked) {
                    R.drawable.ic_like_filled
                } else R.drawable.ic_like,
                0,
                0,
                0
            )
        }
    }

    private fun initMenuButton(post: PostModel) {
        binding.postDetailsMenuOptions.setOnClickListener {
            onMenuClicked(post, null, DiscussionsRecyclerAdapter.DISCUSSION_TYPE_POST)
        }
    }

    private fun likePost(isLiked: Boolean, btnLikeStatus: Boolean) {
        postLikeStatus = isLiked
        likeBtnStatus = btnLikeStatus
    }

    private fun paginatedFlow() {
        binding.postDetailsCommentsRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val layoutManager: RecyclerView.LayoutManager? = recyclerView.layoutManager
                val lastVisibleItemPosition =
                    (layoutManager as LinearLayoutManager?)!!.findLastVisibleItemPosition()

                if (commentsViewModel.hasMoreComments.value!!
                    && commentsViewModel.paginationStatus.value == Constants.PAGE_IDLE
                    && lastVisibleItemPosition != RecyclerView.NO_POSITION
                    // api call when 4 items are left to be seen
                    && lastVisibleItemPosition >= commentsAdapter.itemCount - Constants.COMMENTS_PAGING_SIZE / 2
                ) {
                    commentsViewModel.getComments(this@PostDetailsActivity, postId, null)
                }
            }
        })
    }

    private fun getComments(post: PostModel) {
        //TODO handle get comments in detail activities
        //resetting fetch comment type on refresh all comments
        CommentControllers.commentType = Constants.COMMENT_TYPE_POST

        binding.postDetailsCommentsPb.visibility = View.VISIBLE
        binding.postDetailsCommentsRv.visibility = View.GONE
        commentsViewModel.commentsList.observe(this) {
            if (it != null) {
                commentsAdapter.submitList(it) {
                    if (CommentsViewModel.commentsScrollToTop)
                        binding.postDetailsCommentsRv.scrollToPosition(0)
                }
                //hiding all loading
                binding.postDetailsSwipeRefresh.isRefreshing = false
                binding.postDetailsCommentsPb.visibility = View.GONE
                binding.postDetailsCommentsLottie.visibility = View.GONE
                binding.postDetailsCommentsRv.visibility = View.VISIBLE

                //when empty list is loaded
                if (it.isEmpty()) {
                    binding.postDetailsCommentsLottie.visibility = View.VISIBLE
                    binding.postDetailsCommentsRv.visibility = View.GONE

                    val error = commentsViewModel.isCommentsFetched.value

                    //when empty list is due to network error
                    if (error != Constants.API_SUCCESS && error != null) {
                        Toast.makeText(
                            this, error, Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (error == Constants.AUTH_FAILURE_ERROR) {
                        setResult(Constants.RESULT_LOGOUT)
                        finish()
                    }
                }
            }
        }

        commentsViewModel.getComments(this, post.postId, null)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (postLikeStatus != likeBtnStatus) {
            viewModel.likePost(this, postId)
        }
        finish()
    }

    override fun onCommentLikeChanged(commentId: String, isLiked: Boolean, btnLikeStatus: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            commentLikeHandler.removeCallbacksAndMessages(commentId)
            commentLikeHandler.postDelayed({
                if (isLiked == btnLikeStatus) {
                    commentsViewModel.likeComment(this, commentId)
                }
            }, commentId, Constants.LIKE_DEBOUNCE_TIME)
        } else {
            commentLikeHandler.removeCallbacksAndMessages(null)
            commentLikeHandler.postDelayed({
                if (isLiked == btnLikeStatus) {
                    commentsViewModel.likeComment(this, commentId)
                }
            }, Constants.LIKE_DEBOUNCE_TIME)
        }
    }

    override fun onCommentDeleted(comment: CommentModel) {
        CommentControllers.commentDeleteHandler(this, commentsViewModel, comment)
    }

    override fun onCommentReply(commentId: String, username: String) {
        CommentControllers.commentId = commentId
        CommentControllers.commentType = Constants.COMMENT_TYPE_REPLY

        binding.postDetailsCommentActionsCv.visibility = View.VISIBLE
        binding.postDetailsCommentActionTypeTv.text = getString(R.string.comment_action_label_reply)
        binding.postDetailsCommentActionContentTv.text = username
        binding.postDetailsCommentReplyCancelBtn.setOnClickListener {
            binding.postDetailsCommentActionsCv.visibility = View.GONE
            //restoring comment type
            CommentControllers.commentType = Constants.COMMENT_TYPE_POST
        }
    }

    override fun onCommentEdit(commentId: String, content: String) {
        CommentControllers.commentId = commentId
        CommentControllers.commentType = Constants.COMMENT_TYPE_EDIT

        binding.postDetailsCommentActionsCv.visibility = View.VISIBLE
        binding.postDetailsCommentActionTypeTv.text = getString(R.string.comment_action_label_edit)
        binding.postDetailsCommentActionContentTv.text = content
        binding.postDetailsCommentReplyCancelBtn.setOnClickListener {
            binding.postDetailsCommentActionsCv.visibility = View.GONE
            //restoring comment type
            CommentControllers.commentType = Constants.COMMENT_TYPE_POST
        }
    }

    override fun onCommentCopy(content: String) {
        CommentControllers.commentCopyHandler(this, content)
    }

    override fun onCommentLongClick(comment: CommentModel) {
        val optionsBS = OptionsBS(comment, this@PostDetailsActivity)
        optionsBS.show(supportFragmentManager, optionsBS.tag)
    }

    override fun onMenuClicked(post: PostModel?, poll: PollModel?, type: Int) {
        /*IN USER POSTS ACTIVITY, THE POST ID IS NOT NULL AND THE TYPE IS ALWAYS POST*/

        val optionsBS = DiscussionOptionsBS(post, null, this)
        optionsBS.show(supportFragmentManager, optionsBS.tag)
    }

    override fun onMenuEdit(postId: String?, pollId: String?, type: Int) {
        val intent = Intent(this, CreateEditPostActivity::class.java)
        intent.putExtra(Constants.POST_MODE, Constants.MODE_EDIT_POST)
        intent.putExtra(Constants.POST_ID, postId)
        val post = viewModel.post.value!!
        intent.putExtra(Constants.POST_TITLE, post.title)
        intent.putExtra(Constants.POST_CONTENT, post.content)
        intent.putExtra(Constants.POST_IMAGE, post.postImage)
        startActivity(intent)
    }

    override fun onMenuDelete(postId: String?, pollId: String?, type: Int) {
        /*IN USER POSTS ACTIVITY, THE POST ID IS NOT NULL AND THE TYPE IS ALWAYS POST*/

        MaterialAlertDialogBuilder(this).setTitle("Delete")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Confirm") { dialog, _ ->
                dialog.dismiss()
                deletePost(postId!!)
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    /**
     * METHOD FOR SENDING DELETE POST REQ TO THE VIEW MODEL
     */
    private fun deletePost(postId: String) {
        //post delete api observer
        viewModel.isPostDeleted.observe(this) {
            if (it != null) {
                if (it == Constants.API_SUCCESS) {
                    Toast.makeText(
                        this,
                        "Post Deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else if (it == Constants.API_FAILED) {
                    Toast.makeText(
                        this,
                        "Problem Deleting Post",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        viewModel.deletePost(this, postId)
    }
}