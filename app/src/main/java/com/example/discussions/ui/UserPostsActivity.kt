package com.example.discussions.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.adapters.UserPostsRecyclerAdapter
import com.example.discussions.databinding.ActivityUserPostsBinding
import com.example.discussions.viewModels.UserPostsViewModel

class UserPostsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserPostsBinding
    private lateinit var viewModel: UserPostsViewModel

    private lateinit var userPostsAdapter: UserPostsRecyclerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_posts)
        viewModel = ViewModelProvider(this)[UserPostsViewModel::class.java]

        binding.userPostsRv.apply {
            userPostsAdapter = UserPostsRecyclerAdapter()
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
        viewModel.getUserPosts()
    }
}