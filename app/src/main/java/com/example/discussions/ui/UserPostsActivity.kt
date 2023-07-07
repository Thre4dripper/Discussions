package com.example.discussions.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.adapters.DiscussionsRecyclerAdapter
import com.example.discussions.adapters.interfaces.DiscussionMenuInterface
import com.example.discussions.adapters.interfaces.LikeCommentInterface
import com.example.discussions.adapters.interfaces.PostClickInterface
import com.example.discussions.databinding.ActivityUserPostsBinding
import com.example.discussions.models.PollModel
import com.example.discussions.models.PostModel
import com.example.discussions.repositories.PostRepository
import com.example.discussions.ui.bottomSheets.DiscussionOptionsBS
import com.example.discussions.ui.bottomSheets.comments.CommentsBS
import com.example.discussions.viewModels.PostsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class UserPostsActivity : AppCompatActivity(), PostClickInterface, LikeCommentInterface,
    DiscussionMenuInterface {
    private val TAG = "UserPostsActivity"

    private lateinit var binding: ActivityUserPostsBinding
    private lateinit var viewModel: PostsViewModel

    private lateinit var userPostsAdapter: DiscussionsRecyclerAdapter
    private var handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_posts)
        viewModel = ViewModelProvider(this)[PostsViewModel::class.java]

        binding.userPostsRv.apply {
            userPostsAdapter =
                DiscussionsRecyclerAdapter(
                    this@UserPostsActivity,
                    this@UserPostsActivity,
                    null,
                    this@UserPostsActivity
                )
            adapter = userPostsAdapter
        }

        //getting user id from intent
        val userId = intent.getStringExtra(Constants.USER_ID)!!

        //getting username from intent
        val username = intent.getStringExtra(Constants.USERNAME)
        binding.userPostsHeaderTv.text = getString(R.string.user_post_label, username)

        //getting post id from intent
        val postId = intent.getStringExtra(Constants.POST_ID)
        PostsViewModel.postsScrollToIndex = true

        //getting post index from post id
        val postIndex =
            if (postId == null)
                0
            else
                viewModel.userPostsList.value?.indexOfFirst { it.post?.postId == postId } ?: 0

        binding.userPostsSwipeLayout.setOnRefreshListener {
            viewModel.refreshUserPosts()
            viewModel.getAllUserPosts(this, userId)
        }

        paginatedFlow(userId)

        if (viewModel.userPostsList.value == null) {
            viewModel.refreshUserPosts()
            viewModel.getAllUserPosts(this, userId)
        }

        //only first time scroll to index
        PostsViewModel.postsScrollToIndex = true
        getUserPosts(postIndex)
    }

    private fun paginatedFlow(userId: String) {
        binding.userPostsRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val layoutManager: RecyclerView.LayoutManager? = recyclerView.layoutManager
                val lastVisibleItemPosition =
                    (layoutManager as LinearLayoutManager?)!!.findLastVisibleItemPosition()

                if (PostsViewModel.hasMorePosts.value!!
                    && PostsViewModel.paginationStatus.value == Constants.PAGE_IDLE
                    && lastVisibleItemPosition != RecyclerView.NO_POSITION
                    // api call when 4 items are left to be seen
                    && lastVisibleItemPosition >= userPostsAdapter.itemCount - Constants.POSTS_PAGING_SIZE / 2
                ) {
                    viewModel.getAllUserPosts(this@UserPostsActivity, userId)
                }
            }
        })
    }

    /**
     * METHOD FOR GETTING USER POSTS FROM POST REPOSITORY AND POPULATING THE RECYCLER VIEW
     */
    private fun getUserPosts(postIndex: Int) {
        binding.userPostsProgressBar.visibility = View.VISIBLE
        viewModel.userPostsList.observe(this) {
            if (it != null) {
                userPostsAdapter.submitList(it) {
                    if (PostsViewModel.postsScrollToIndex) {
                        binding.userPostsRv.scrollToPosition(postIndex)
                        PostsViewModel.postsScrollToIndex = false
                    }
                }
                //hiding all loading
                binding.userPostsSwipeLayout.isRefreshing = false
                binding.userPostsProgressBar.visibility = View.GONE
                binding.userPostsLottieNoData.visibility = View.GONE

                //when empty list is loaded
                if (it.isEmpty()) {
                    binding.userPostsLottieNoData.visibility = View.VISIBLE
                    val error = viewModel.isUserPostsFetched.value

                    //when empty list is due to network error
                    if (error != Constants.API_SUCCESS && error != null) {
                        Toast.makeText(
                            this,
                            viewModel.isUserPostsFetched.value,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    //todo handle all these errors
                    if (error == Constants.AUTH_FAILURE_ERROR) {
                        setResult(Constants.RESULT_LOGOUT)
                        finish()
                    }
                }
            }
        }
    }


    override fun onPostClick(postId: String) {
        val intent = Intent(this, PostDetailsActivity::class.java)
        intent.putExtra(Constants.POST_ID, postId)
        startActivity(intent)
    }

    override fun onLike(
        postId: String?,
        pollId: String?,
        type: Int,
        isLiked: Boolean,
        btnLikeStatus: Boolean
    ) {
        /*IN USER POSTS ACTIVITY, THE POST ID IS NOT NULL AND THE TYPE IS ALWAYS POST*/

        viewModel.isPostLikedChanged.observe(this) {
            if (it != null) {
                if (it == Constants.API_FAILED) {
                    Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                } else if (it == Constants.AUTH_FAILURE_ERROR) {
                    setResult(Constants.RESULT_LOGOUT)
                    finish()
                }
            }
        }

        //debouncing the like button above android P
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            handler.removeCallbacksAndMessages(postId)
            handler.postDelayed({
                if (isLiked == btnLikeStatus)
                    viewModel.likePost(this, postId!!)

            }, postId, Constants.LIKE_DEBOUNCE_TIME)
        }
        //debouncing the like button below android P
        else {
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                if (isLiked == btnLikeStatus)
                    viewModel.likePost(this, postId!!)

            }, Constants.LIKE_DEBOUNCE_TIME)
        }
    }

    override fun onComment(postId: String?, pollId: String?, type: Int) {
        /*IN USER POSTS ACTIVITY, THE POST ID IS NOT NULL AND THE TYPE IS ALWAYS POST*/

        val count =
            PostRepository.userPostsList.value?.find { it.post!!.postId == postId }?.post?.comments
                ?: 0

        val commentsBS = CommentsBS(postId!!, Constants.COMMENT_TYPE_POST, count)
        commentsBS.show(this.supportFragmentManager, commentsBS.tag)
    }

    override fun onMenuClicked(post: PostModel?, poll: PollModel?, type: Int) {
        /*IN USER POSTS ACTIVITY, THE POST ID IS NOT NULL AND THE TYPE IS ALWAYS POST*/

        val optionsBs = DiscussionOptionsBS(post, null, this@UserPostsActivity)
        optionsBs.show(supportFragmentManager, optionsBs.tag)
    }

    override fun onMenuEdit(postId: String?, pollId: String?, type: Int) {
        val intent = Intent(this, CreateEditPostActivity::class.java)
        intent.putExtra(Constants.POST_MODE, Constants.MODE_EDIT_POST)
        intent.putExtra(Constants.POST_ID, postId)
        val post = viewModel.userPostsList.value?.find { it.post!!.postId == postId }!!.post!!
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
                if (it == Constants.API_SUCCESS) Toast.makeText(
                    this,
                    "Post Deleted",
                    Toast.LENGTH_SHORT
                ).show()
                else if (it == Constants.API_FAILED) Toast.makeText(
                    this,
                    "Problem Deleting Post",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        viewModel.deletePost(this, postId)
    }
}