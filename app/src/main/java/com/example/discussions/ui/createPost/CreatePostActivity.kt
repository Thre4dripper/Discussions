package com.example.discussions.ui.createPost

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
            createPost()
        }

        binding.createPostAddImageBtn.setOnClickListener {
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.createPostClearImageBtn.setOnClickListener { clearPostImage() }

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

    // Registers a photo picker activity launcher in single-select mode.
    private val photoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia())
        { pickedPhotoUri ->
            if (pickedPhotoUri != null) {
                var uri: Uri? = null

                //async call to create temp file
                CoroutineScope(Dispatchers.Main).launch {
                    uri = Uri.fromFile(withContext(Dispatchers.IO) {
                        val file = File.createTempFile( //creating temp file
                            "temp", ".jpg", cacheDir
                        )
                        file
                    })
                }
                    //on completion of async call
                    .invokeOnCompletion {
                        //Crop activity with source and destination uri
                        val uCrop = UCrop.of(pickedPhotoUri, uri!!).withAspectRatio(1f, 1f)
                            .withMaxResultSize(1080, 1080)

                        cropImageCallback.launch(uCrop.getIntent(this))
                    }

            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            }
        }

    /**
     * CALLBACK FOR CROPPING RECEIVED IMAGE
     */
    private var cropImageCallback =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                viewModel.postImage = UCrop.getOutput(it.data!!).toString()

                //setting image to image view
                Glide.with(this).load(viewModel.postImage).into(binding.createPostImage)

                //setting visibility of delete button
                binding.createPostClearImageBtn.visibility = View.VISIBLE
            }
        }

    private fun clearPostImage() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Clear Image")
            .setMessage("Are you sure you want to clear the image?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.postImage = null
                binding.createPostImage.setImageDrawable(null)
                binding.createPostClearImageBtn.visibility = View.GONE
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun createPost() {
        viewModel.postTitle = binding.createPostTitle.text.toString()
        viewModel.postContent = binding.createPostContent.text.toString()
        viewModel.allowComments = binding.createPostCb.isChecked

        if (viewModel.postTitle.isEmpty() && viewModel.postContent.isEmpty() && viewModel.postImage == null) {
            Toast.makeText(this, "No content to post", Toast.LENGTH_SHORT).show()
            return
        }

        loadingDialog.show()

        viewModel.isPostCreated.observe(this) {
            if (it != null) {
                loadingDialog.dismiss()

                if (it == Constants.API_SUCCESS) {
                    Toast.makeText(this, "Post created", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error creating post", Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.createPost(this)
    }
}