package com.example.discussions.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
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
        getUserPosts()
    }

    private fun getUserPosts() {
        viewModel.userPosts.observe(this) {
            userPostsAdapter.submitList(it)
        }
        viewModel.getUserPosts()
    }
}