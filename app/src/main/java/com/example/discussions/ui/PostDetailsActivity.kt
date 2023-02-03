package com.example.discussions.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.adapters.CommentsRecyclerAdapter
import com.example.discussions.adapters.interfaces.CommentInterface
import com.example.discussions.databinding.ActivityPostDetailsBinding
import com.example.discussions.models.CommentModel
import com.example.discussions.models.PostModel
import com.example.discussions.ui.bottomSheets.comments.CommentControllers
import com.example.discussions.ui.bottomSheets.comments.OptionsBS
import com.example.discussions.viewModels.CommentsViewModel
import com.example.discussions.viewModels.PostDetailsViewModel
import java.text.SimpleDateFormat
import java.util.*

class PostDetailsActivity : AppCompatActivity(), CommentInterface {
    private val TAG = "PostDetailsActivity"

    private lateinit var binding: ActivityPostDetailsBinding
    private lateinit var viewModel: PostDetailsViewModel
    private lateinit var commentsViewModel: CommentsViewModel

    private lateinit var commentsAdapter: CommentsRecyclerAdapter
    private var commentLikeHandler = Handler(Looper.getMainLooper())
    private var commentType = Constants.COMMENT_TYPE_POST
    private var commentId: String? = null

    private var postId = ""
    private var postLikeStatus = false
    private var likeBtnStatus = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_post_details)

        viewModel = ViewModelProvider(this)[PostDetailsViewModel::class.java]
        commentsViewModel = ViewModelProvider(this)[CommentsViewModel::class.java]

        //get post id from intent
        postId = intent.getStringExtra(Constants.POST_ID)!!

        binding.postDetailsBackBtn.setOnClickListener {
            onBackPressed()
        }
        getPost(postId)
    }

    private fun getPost(postId: String) {
        //check if post is in post list
        if (viewModel.isPostInAlreadyFetched(postId)) {
            //if yes, get post from post repository
            viewModel.getPostFromPostRepository(postId)
            setDetails()
        } else {
            //if not, get post from server
//            viewModel.getPostFromServer(postId)
        }
    }

    private fun setDetails() {
        val post = viewModel.post
        setUserInfo(post)
        setPostData(post)
        initLikeButton(post)

        addCommentHandler()

        binding.postDetailsCommentsRv.apply {
            commentsAdapter = CommentsRecyclerAdapter(this@PostDetailsActivity)
            adapter = commentsAdapter
        }

        binding.postDetailsSwipeRefresh.setOnRefreshListener {
            getComments(post)
        }
        getComments(post)
    }

    private fun setUserInfo(post: PostModel) {
        //set user Image
        Glide.with(this)
            .load(post.userImage)
            .placeholder(R.drawable.ic_profile)
            .circleCrop()
            .into(binding.postDetailsUserImage)

        //set user name and time
        binding.postDetailsUsername.text = viewModel.post.username
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = dateFormat.parse(post.createdAt)

        binding.postDetailsTime.text = DateUtils.getRelativeTimeSpanString(
            date!!.time,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )
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

    private fun likePost(isLiked: Boolean, btnLikeStatus: Boolean) {
        postLikeStatus = isLiked
        likeBtnStatus = btnLikeStatus
    }

    private fun addCommentHandler() {
        //preconfiguring add comment button
        binding.postDetailsAddCommentBtn.apply {
            isEnabled = false
            drawable.alpha = 100
            setOnClickListener {
                createEditComment(binding.postDetailsAddCommentEt.text.toString())
                binding.postDetailsAddCommentEt.text.clear()
            }
        }

        //controlling add comment button based on text
        binding.postDetailsAddCommentEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    binding.postDetailsAddCommentBtn.apply {
                        isEnabled = false
                        drawable.alpha = 100
                    }
                } else {
                    binding.postDetailsAddCommentBtn.apply {
                        isEnabled = true
                        drawable.alpha = 255
                    }
                }
            }

        })
    }

    private fun createEditComment(content: String) {
        binding.postDetailsCommentAddProgressBar.visibility = View.VISIBLE
        binding.postDetailsAddCommentBtn.visibility = View.GONE

        when (commentType) {
            Constants.COMMENT_TYPE_POST -> commentsViewModel.createComment(
                this, postId = postId, null, null, content
            )
            Constants.COMMENT_TYPE_REPLY -> commentsViewModel.createComment(
                this, null, null, commentId = commentId, content
            )
            Constants.COMMENT_TYPE_EDIT -> commentsViewModel.editComment(
                this, commentId!!, content
            )
        }
    }

    private fun getComments(post: PostModel) {
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

        commentsViewModel.getComments(this, post.postId,null)
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
        this.commentId = commentId
        commentType = Constants.COMMENT_TYPE_REPLY

        binding.postDetailsCommentActionsCv.visibility = View.VISIBLE
        binding.postDetailsCommentActionTypeTv.text = getString(R.string.comment_action_label_reply)
        binding.postDetailsCommentActionContentTv.text = username
        binding.postDetailsCommentReplyCancelBtn.setOnClickListener {
            binding.postDetailsCommentActionsCv.visibility = View.GONE
            //restoring comment type
            commentType = Constants.COMMENT_TYPE_POST
        }
    }

    override fun onCommentEdit(commentId: String, content: String) {
        this.commentId = commentId
        commentType = Constants.COMMENT_TYPE_REPLY

        binding.postDetailsCommentActionsCv.visibility = View.VISIBLE
        binding.postDetailsCommentActionTypeTv.text = getString(R.string.comment_action_label_edit)
        binding.postDetailsCommentActionContentTv.text = content
        binding.postDetailsCommentReplyCancelBtn.setOnClickListener {
            binding.postDetailsCommentActionsCv.visibility = View.GONE
            //restoring comment type
            commentType = Constants.COMMENT_TYPE_POST
        }
    }

    override fun onCommentCopy(content: String) {
        CommentControllers.commentCopyHandler(this, content)
    }

    override fun onCommentLongClick(comment: CommentModel) {
        val optionsBS = OptionsBS(comment, this@PostDetailsActivity)
        optionsBS.show(supportFragmentManager, optionsBS.tag)
    }
}