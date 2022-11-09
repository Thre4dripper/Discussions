package com.example.discussions.ui.createPost

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.databinding.ActivityCreatePostBinding
import com.example.discussions.viewModels.CreatePostViewModel

class CreatePostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreatePostBinding
    private lateinit var viewModel: CreatePostViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_post)
        viewModel = ViewModelProvider(this)[CreatePostViewModel::class.java]

        binding.createPostBackBtn.setOnClickListener {
            finish()
        }
        binding.createPostBtn.setOnClickListener {
//            createPost()
        }

        getUserData()
    }

    private fun getUserData() {
        viewModel.profileImage = intent.getStringExtra(Constants.PROFILE_IMAGE)
        viewModel.username = intent.getStringExtra(Constants.USERNAME)

        Glide.with(this)
            .load(viewModel.profileImage)
            .placeholder(R.drawable.ic_profile)
            .circleCrop()
            .into(binding.createPostProfileIv)

        binding.createPostUsernameTv.text = viewModel.username
    }

    private fun createPost() {
        viewModel.postTitle = binding.createPostTitle.text.toString()
        viewModel.postContent = binding.createPostContent.text.toString()
        viewModel.allowComments = binding.createPostCb.isChecked

        finish()
    }
}