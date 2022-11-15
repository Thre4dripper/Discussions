package com.example.discussions.ui.createPost

import android.content.Intent
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
import com.example.discussions.Cloudinary
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.api.ResponseCallback
import com.example.discussions.databinding.ActivityCreatePostBinding
import com.example.discussions.databinding.LoadingDialogBinding
import com.example.discussions.ui.zoomImage.ZoomImageActivity
import com.example.discussions.viewModels.CreatePostViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CreatePostActivity : AppCompatActivity() {
    private val TAG = "CreatePostActivity"
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
        binding.createPostProfileIv.setOnClickListener {
            val intent = Intent(this, ZoomImageActivity::class.java)
            intent.putExtra(Constants.ZOOM_IMAGE_URL, viewModel.profileImage)
            startActivity(intent)
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
                        val uCrop = UCrop.of(pickedPhotoUri, uri!!)

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
        viewModel.postTitle = binding.createPostTitle.text.toString().trim()
        viewModel.postContent = binding.createPostContent.text.toString().trim()
        viewModel.allowComments = binding.createPostCb.isChecked

        //either title or content is necessary to create a post
        //or Image is necessary to create a post
        if (binding.createPostImage.drawable == null) {
            Toast.makeText(this, "or Select an image", Toast.LENGTH_SHORT).show()
            if (viewModel.postTitle.isEmpty()) {
                binding.createPostTitle.error = "Title cannot be empty"
                binding.createPostTitle.requestFocus()
                return
            }

            if (viewModel.postTitle.length < 10) {
                binding.createPostTitle.error = "Title must be at least 5 characters long"
                binding.createPostTitle.requestFocus()
                return
            }

            if (viewModel.postContent.isEmpty()) {
                binding.createPostContent.error = "Content cannot be empty"
                binding.createPostContent.requestFocus()
                return
            }

            if (viewModel.postContent.length < 20) {
                binding.createPostContent.error = "Content must be at least 10 characters long"
                binding.createPostContent.requestFocus()
                return
            }
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

        //firstly, post image is uploaded to cloudinary after which post is created with the url of the image received from cloudinary
        //if no image is selected, post is created directly with fallback image url
        Cloudinary.uploadImage(
            object : ResponseCallback {
                override fun onSuccess(response: String) {
                    viewModel.postImage = response
                    viewModel.createPost(this@CreatePostActivity)
                }

                override fun onError(response: String) {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this@CreatePostActivity, response, Toast.LENGTH_SHORT
                    ).show()
                }

            },
            selectedImageUri = Uri.parse(viewModel.postImage ?: ""),
            fallbackImageUri = Uri.EMPTY,
            folderName = viewModel.username
        )
    }
}