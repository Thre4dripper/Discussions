package com.example.discussions.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.adapters.UserPostsRecyclerAdapter
import com.example.discussions.databinding.ActivityUserPostsBinding
import com.example.discussions.viewModels.UserPostsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class UserPostsActivity : AppCompatActivity(), UserPostsRecyclerAdapter.PostOptionsInterface {
    private val TAG = "UserPostsActivity"

    private lateinit var binding: ActivityUserPostsBinding
    private lateinit var viewModel: UserPostsViewModel

    private lateinit var userPostsAdapter: UserPostsRecyclerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_posts)
        viewModel = ViewModelProvider(this)[UserPostsViewModel::class.java]

        binding.userPostsRv.apply {
            userPostsAdapter = UserPostsRecyclerAdapter(this@UserPostsActivity)
            adapter = userPostsAdapter
        }

        //getting user posts from view model
        val username = intent.getStringExtra(Constants.USERNAME)
        binding.userPostsHeaderTv.text = getString(R.string.user_post_label, username)

        //getting post index from intent
        val postIndex = intent.getIntExtra(Constants.USER_POST_INDEX, 0)

        getUserPosts(postIndex)
    }

    /**
     * METHOD FOR GETTING USER POSTS FROM POST REPOSITORY AND POPULATING THE RECYCLER VIEW
     */
    private fun getUserPosts(postIndex: Int) {
        viewModel.userPosts.observe(this) {
            userPostsAdapter.submitList(it) {
                binding.userPostsRv.scrollToPosition(postIndex)
            }
        }
    }

    override fun onPostEdit(postId: String) {
        Log.d(TAG, "onPostEdit: $postId")
    }

    override fun onPostDelete(postId: String) {

        MaterialAlertDialogBuilder(this)
            .setTitle("Delete")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Confirm") { dialog, _ ->
                dialog.dismiss()
                deletePost(postId)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * METHOD FOR SENDING DELETE POST REQ TO THE VIEW MODEL
     */
    private fun deletePost(postId: String) {
        //post delete api observer
        viewModel.isPostDeleted.observe(this) {
            if (it != null) {
                if (it == Constants.API_SUCCESS)
                    Toast.makeText(this, "Post Deleted", Toast.LENGTH_SHORT).show()
                else if (it == Constants.API_FAILED)
                    Toast.makeText(this, "Problem Deleting Post", Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.deletePost(this, postId)
    }
}