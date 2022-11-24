package com.example.discussions.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.adapters.PollOptionsRecyclerAdapter
import com.example.discussions.databinding.ActivityCreatePollBinding
import com.example.discussions.databinding.LoadingDialogBinding
import com.example.discussions.viewModels.CreatePollViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CreatePollActivity : AppCompatActivity(),
    PollOptionsRecyclerAdapter.PollOptionClickInterface {
    private lateinit var binding: ActivityCreatePollBinding
    private lateinit var viewModel: CreatePollViewModel

    private lateinit var loadingDialog: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_poll)
        viewModel = ViewModelProvider(this)[CreatePollViewModel::class.java]

        //initializing back button
        binding.createPollBackBtn.setOnClickListener {
            finish()
        }

        //initializing poll creation button
        binding.createPollBtn.setOnClickListener {
            createPoll()
        }

        //initializing user profile image click to zoom in button
        binding.createPollProfileIv.setOnClickListener {
            val intent = Intent(this, ZoomImageActivity::class.java)
            intent.putExtra(Constants.ZOOM_IMAGE_URL, viewModel.profileImage)
            startActivity(intent)
        }

        binding.createPollAddOptionBtn.setOnClickListener {
            viewModel.addPollOption()
        }

        initLoadingDialog()
        initUsernameAndImage()
        initPollOptionsRv()
    }

    /**
     * METHOD FOR INITIALIZING LOADING DIALOG
     */
    private fun initLoadingDialog() {
        val dialogBinding = LoadingDialogBinding.inflate(layoutInflater)
        loadingDialog = MaterialAlertDialogBuilder(this).setView(dialogBinding.root)
            .setCancelable(false).show()
        loadingDialog.dismiss()
    }

    /**
     * METHOD FOR INITIALIZING USERNAME AND PROFILE IMAGE
     */
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
                        .into(binding.createPollProfileIv)

                    binding.createPollUsernameTv.text = viewModel.username
                } else {
                    Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.getUsernameAndImage(this)
    }

    private fun initPollOptionsRv() {
        val adapter = PollOptionsRecyclerAdapter(this)
        binding.createPollOptionsRv.adapter = adapter

        viewModel.pollOptions.observe(this) {
            adapter.submitList(it)

            //at max 6 options can be added
            binding.createPollAddOptionBtn.visibility =
                if (it.size >= 6) View.GONE else View.VISIBLE

        }
    }

    override fun onPollOptionDelete(position: Int) {
        viewModel.deletePollOption(position)
    }

    override fun onPollTextChanged(position: Int, text: String) {
        viewModel.updatePollOption(position, text)
    }

    /**
     * METHOD FOR CREATING POLL
     */
    private fun createPoll() {
        viewModel.pollTitle = binding.createPollTitle.text.toString().trim()
        viewModel.pollContent = binding.createPollContent.text.toString().trim()
        viewModel.isPrivate = binding.createPollPrivateCb.isChecked
        viewModel.allowComments = binding.createPollAllowCommentsCb.isChecked

        //poll title field checking
        if (viewModel.pollTitle.isEmpty()) {
            binding.createPollTitle.error = "Title cannot be empty"
            binding.createPollTitle.requestFocus()
            return
        }

        if (viewModel.pollTitle.length < 5) {
            binding.createPollTitle.error = "Title must be at least 5 characters long"
            binding.createPollTitle.requestFocus()
            return
        }

        //poll content field checking
        if (viewModel.pollContent.isEmpty()) {
            binding.createPollContent.error = "Content cannot be empty"
            binding.createPollContent.requestFocus()
            return
        }

        if (viewModel.pollContent.length < 10) {
            binding.createPollContent.error = "Content must be at least 10 characters long"
            binding.createPollContent.requestFocus()
            return
        }

        // checking number of poll options
        if (viewModel.pollOptions.value!!.size < 2) {
            Toast.makeText(this, "At least 2 options are required", Toast.LENGTH_SHORT).show()
            return
        }

        //checking if all poll options are filled
        viewModel.pollOptions.value!!.forEach {
            if (it.content.isEmpty()) {
                Toast.makeText(this, "Options cannot be empty", Toast.LENGTH_SHORT).show()
                return
            }
        }

        //checking if all poll options are unique
        val uniqueOptions = viewModel.pollOptions.value!!.map { it.content.trim() }.distinct()
        if (uniqueOptions.size < viewModel.pollOptions.value!!.size) {
            Toast.makeText(this, "Duplicate options are not allowed", Toast.LENGTH_SHORT).show()
            return
        }

        /* AFTER ALL CHECKS PASSED */
        loadingDialog.show()

        viewModel.isPollCreated.observe(this) {
            if (it != null) {
                loadingDialog.dismiss()

                if (it == Constants.API_SUCCESS) {
                    Toast.makeText(this, "Poll created successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error creating poll", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.createPoll(this)
    }
}