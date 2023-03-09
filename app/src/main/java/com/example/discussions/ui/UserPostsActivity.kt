package com.example.discussions.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.adapters.DiscussionsRecyclerAdapter
import com.example.discussions.adapters.interfaces.DiscussionMenuInterface
import com.example.discussions.adapters.interfaces.LikeCommentInterface
import com.example.discussions.adapters.interfaces.PostClickInterface
import com.example.discussions.databinding.ActivityUserPostsBinding
import com.example.discussions.repositories.DiscussionRepository
import com.example.discussions.ui.bottomSheets.comments.CommentsBS
import com.example.discussions.viewModels.UserPostsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class UserPostsActivity : AppCompatActivity(), PostClickInterface, LikeCommentInterface,
    DiscussionMenuInterface {
    private val TAG = "UserPostsActivity"

    private lateinit var binding: ActivityUserPostsBinding
    private lateinit var viewModel: UserPostsViewModel

    private lateinit var userPostsAdapter: DiscussionsRecyclerAdapter
    private var handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_posts)
        viewModel = ViewModelProvider(this)[UserPostsViewModel::class.java]

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

        //getting user posts from view model
        val username = intent.getStringExtra(Constants.USERNAME)
        binding.userPostsHeaderTv.text = getString(R.string.user_post_label, username)

        //getting post id from intent
        val postId = intent.getStringExtra(Constants.POST_ID)!!
        UserPostsViewModel.userPostsScrollToIndex = true

        //getting post index from post id
        val postIndex = viewModel.userPosts.value?.indexOfFirst { it.post!!.postId == postId }!!
        getUserPosts(postIndex)
    }

    /**
     * METHOD FOR GETTING USER POSTS FROM POST REPOSITORY AND POPULATING THE RECYCLER VIEW
     */
    private fun getUserPosts(postIndex: Int) {
        viewModel.userPosts.observe(this) {
            userPostsAdapter.submitList(it) {
                if (UserPostsViewModel.userPostsScrollToIndex) binding.userPostsRv.scrollToPosition(
                    postIndex
                )
            }
        }
    }

    //TODO implement bottom sheet for these operations

    override fun onPostEdit(postId: String) {
        val intent = Intent(this, CreateEditPostActivity::class.java)
        intent.putExtra(Constants.POST_MODE, Constants.MODE_EDIT_POST)
        intent.putExtra(Constants.POST_ID, postId)
        val post = viewModel.userPosts.value?.find { it.post!!.postId == postId }!!.post!!
        intent.putExtra(Constants.POST_TITLE, post.title)
        intent.putExtra(Constants.POST_CONTENT, post.content)
        intent.putExtra(Constants.POST_IMAGE, post.postImage)
        startActivity(intent)
    }

    override fun onPostDelete(postId: String) {
        MaterialAlertDialogBuilder(this).setTitle("Delete")
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

    override fun onPostLike(postId: String, isLiked: Boolean, btnLikeStatus: Boolean) {
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
                    viewModel.likePost(this, postId)

            }, postId, Constants.LIKE_DEBOUNCE_TIME)
        }
        //debouncing the like button below android P
        else {
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                if (isLiked == btnLikeStatus)
                    viewModel.likePost(this, postId)

            }, Constants.LIKE_DEBOUNCE_TIME)
        }
    }

    override fun onPostComment(postId: String) {
        val count =
            DiscussionRepository.discussions.value?.find { it.post!!.postId == postId }?.count ?: 0

        val commentsBS = CommentsBS(postId, Constants.COMMENT_TYPE_POST, count)
        commentsBS.show(this.supportFragmentManager, commentsBS.tag)
    }

    override fun onPostClick(postId: String) {

    }
}