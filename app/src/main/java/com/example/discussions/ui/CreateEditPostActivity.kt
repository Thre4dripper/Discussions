package com.example.discussions.ui

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
import com.example.discussions.databinding.ActivityCreateEditPostBinding
import com.example.discussions.databinding.LoadingDialogBinding
import com.example.discussions.viewModels.CreateEditPostViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CreateEditPostActivity : AppCompatActivity() {
    private val TAG = "CreatePostActivity"
    private lateinit var binding: ActivityCreateEditPostBinding
    private lateinit var viewModel: CreateEditPostViewModel

    private lateinit var loadingDialog: AlertDialog
    private lateinit var postMode: String
    private lateinit var postId: String
    private lateinit var postImageFallbackUri: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_edit_post)
        viewModel = ViewModelProvider(this)[CreateEditPostViewModel::class.java]

        //initializing back button
        binding.createPostBackBtn.setOnClickListener {
            finish()
        }
        //initializing post creation button
        binding.createPostBtn.setOnClickListener {
            createPost()
        }

        //initializing user profile image click to zoom in button
        binding.createPostProfileIv.setOnClickListener {
            val intent = Intent(this, ZoomImageActivity::class.java)
            intent.putExtra(Constants.ZOOM_IMAGE_URL, viewModel.profileImage)
            startActivity(intent)
        }

        //initializing image selection button
        binding.createPostAddImageBtn.setOnClickListener {
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        //initializing clear image button
        binding.createPostClearImageBtn.setOnClickListener { clearPostImage() }

        initLoadingDialog()
        initUsernameAndImage()

        //getting post mode from intent
        postMode = intent.getStringExtra(Constants.POST_MODE)!!

        initPostModeChanges()
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
                        .into(binding.createPostProfileIv)

                    binding.createPostUsernameTv.text = viewModel.username
                } else {
                    Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.getUsernameAndImage(this)
    }

    /**
     * METHOD FOR INITIALIZING POST MODE CHANGES
     */
    private fun initPostModeChanges() {
        when (postMode) {
            //if post mode is create
            Constants.MODE_CREATE_POST -> {
                binding.createPostLabel.text = getString(R.string.create_post_label)
                binding.createPostBtn.text = getString(R.string.create_post_btn_text)

                //fallback uri is empty since there is no image
                postImageFallbackUri = Uri.EMPTY
            }
            //if post mode is edit
            Constants.MODE_EDIT_POST -> {
                binding.createPostLabel.text = getString(R.string.edit_post_label)
                binding.createPostBtn.text = getString(R.string.edit_post_btn_text)

                postId = intent.getStringExtra(Constants.POST_ID)!!
                val postTitle = intent.getStringExtra(Constants.POST_TITLE)!!
                val postBody = intent.getStringExtra(Constants.POST_CONTENT)!!
                val postImage = intent.getStringExtra(Constants.POST_IMAGE)!!

                binding.createPostTitle.setText(postTitle)
                binding.createPostContent.setText(postBody)

                if (postImage != "") {
                    Glide.with(this).load(postImage).into(binding.createPostIv)
                    binding.createPostClearImageBtn.visibility = View.VISIBLE

                    //fallback uri is the previous image uri
                    postImageFallbackUri = Uri.parse(postImage)
                } else {
                    //fallback uri is empty since there is no image
                    postImageFallbackUri = Uri.EMPTY
                }
            }
        }
    }

    /**
     * CALLBACK FOR PHOTO PICKER ACTIVITY
     */
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
                Glide.with(this).load(viewModel.postImage).into(binding.createPostIv)

                //setting visibility of delete button
                binding.createPostClearImageBtn.visibility = View.VISIBLE
            }
        }

    /**
     * METHOD FOR CLEARING POST IMAGE
     */
    private fun clearPostImage() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Clear Image")
            .setMessage("Are you sure you want to clear the image?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.postImage = ""
                binding.createPostIv.setImageDrawable(null)
                binding.createPostClearImageBtn.visibility = View.GONE

                //on clearing post image, delete it from cloudinary also,
                //when new post is created, there is no fallback uri
                if (postImageFallbackUri != Uri.EMPTY)
                    Cloudinary.deleteImage(this, postImageFallbackUri.toString())

                postImageFallbackUri = Uri.EMPTY
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * METHOD FOR CREATING POST
     */
    private fun createPost() {
        viewModel.postTitle = binding.createPostTitle.text.toString().trim()
        viewModel.postContent = binding.createPostContent.text.toString().trim()
        viewModel.allowComments = binding.createPostAllowCommentsCb.isChecked

        //either title or content is necessary to create a post
        //or Image is necessary to create a post
        if (binding.createPostIv.drawable == null) {
            val toast = Toast.makeText(this, "or Select an image", Toast.LENGTH_SHORT)

            if (viewModel.postTitle.isEmpty()) {
                binding.createPostTitle.error = "Title cannot be empty"
                binding.createPostTitle.requestFocus()
                toast.show()
                return
            }

            if (viewModel.postTitle.length < 5) {
                binding.createPostTitle.error = "Title must be at least 5 characters long"
                binding.createPostTitle.requestFocus()
                toast.show()
                return
            }

            if (viewModel.postContent.isEmpty()) {
                binding.createPostContent.error = "Content cannot be empty"
                binding.createPostContent.requestFocus()
                toast.show()
                return
            }

            if (viewModel.postContent.length < 10) {
                binding.createPostContent.error = "Content must be at least 10 characters long"
                binding.createPostContent.requestFocus()
                toast.show()
                return
            }
        }

        /* AFTER ALL CHECKS PASSED */
        loadingDialog.show()

        viewModel.isPostCreatedOrUpdated.observe(this) {
            if (it != null) {
                loadingDialog.dismiss()

                val successToastMsg =
                    if (postMode == Constants.MODE_CREATE_POST) "Post created" else "Post updated"

                val failureToastMsg =
                    if (postMode == Constants.MODE_CREATE_POST) "Error creating post" else "Error updating post"

                if (it == Constants.API_SUCCESS) {
                    Toast.makeText(this, successToastMsg, Toast.LENGTH_SHORT).show()

                    //exit activity after operation is successful
                    finish()
                } else {
                    Toast.makeText(this, failureToastMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        /*
        FIRSTLY, POST IMAGE IS UPLOADED TO CLOUDINARY
        AFTER WHICH POST IS CREATED WITH THE URL OF THE IMAGE RECEIVED FROM CLOUDINARY
        IF NO IMAGE IS SELECTED, POST IS CREATED DIRECTLY WITH FALLBACK IMAGE URL
         */
        Cloudinary.uploadImage(
            this,
            object : ResponseCallback {
                override fun onSuccess(response: String) {
                    viewModel.postImage = response
                    if (postMode == Constants.MODE_CREATE_POST) {
                        viewModel.createPost(this@CreateEditPostActivity)
                    } else if (postMode == Constants.MODE_EDIT_POST) {
                        viewModel.editPost(this@CreateEditPostActivity, postId)
                    }
                }

                override fun onError(response: String) {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this@CreateEditPostActivity, response, Toast.LENGTH_SHORT
                    ).show()
                }

            },
            selectedImageUri = Uri.parse(viewModel.postImage),
            fallbackImageUri = postImageFallbackUri,
            folderName = viewModel.username
        )
    }
}