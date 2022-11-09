package com.example.discussions.ui.createPost

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.databinding.ActivityCreatePostBinding
import com.example.discussions.databinding.LoadingDialogBinding
import com.example.discussions.viewModels.CreatePostViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CreatePostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreatePostBinding
    private lateinit var viewModel: CreatePostViewModel

    private lateinit var loadingDialog: AlertDialog
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

        initLoadingDialog()
        initUsernameAndImage()
    }

    private fun initLoadingDialog() {
        val dialogBinding = LoadingDialogBinding.inflate(layoutInflater)
        loadingDialog = MaterialAlertDialogBuilder(this).setView(dialogBinding.root)
            .setCancelable(false).show()
        loadingDialog.dismiss()
    }

    private fun initUsernameAndImage() {
        loadingDialog.show()
        viewModel.isApiFetched.observe(this) {
            if (it != null) {
                loadingDialog.dismiss()

                if (it == Constants.API_SUCCESS) {
                    Glide.with(this)
                        .load(viewModel.profileImage)
                        .placeholder(R.drawable.ic_profile)
                        .circleCrop()
                        .into(binding.createPostProfileIv)

                    binding.createPostUsernameTv.text = viewModel.username
                } else {
                    Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.getUsernameAndImage(this)
    }

    private fun createPost() {
        viewModel.postTitle = binding.createPostTitle.text.toString()
        viewModel.postContent = binding.createPostContent.text.toString()
        viewModel.allowComments = binding.createPostCb.isChecked

        finish()
    }
}