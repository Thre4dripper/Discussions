package com.example.discussions.ui.editDetails

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.api.ResponseCallback
import com.example.discussions.databinding.ActivityEditDetailsBinding
import com.example.discussions.databinding.LoadingDialogBinding
import com.example.discussions.viewModels.EditDetailsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class EditDetailsActivity : AppCompatActivity() {
    private val TAG = "EditProfileActivity"

    private lateinit var binding: ActivityEditDetailsBinding
    private lateinit var viewModel: EditDetailsViewModel

    private lateinit var loadingDialog: AlertDialog
    private var selectedImageUri: Uri = Uri.EMPTY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_details)
        viewModel = ViewModelProvider(this)[EditDetailsViewModel::class.java]

        binding.editDetailsBackBtn.setOnClickListener {
            finish()
        }
        binding.clearProfileImageBtn.setOnClickListener {
            clearProfileImage()
        }
        binding.editDetailsImageBtn.setOnClickListener {
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.lifecycleOwner = this
        initLoadingDialog()
        initRadioButtons()
        initDOBDialog()

        getDetails()

        binding.updateProfileBtn.setOnClickListener {
            updateDetails()
        }
    }

    private fun initLoadingDialog() {
        val dialogBinding = LoadingDialogBinding.inflate(layoutInflater)
        loadingDialog = MaterialAlertDialogBuilder(this).setView(dialogBinding.root)
            .setCancelable(false).show()
        loadingDialog.dismiss()
    }

    /**
     * METHOD FOR INITIALIZING RADIO BUTTONS CLICK LISTENERS
     */
    private fun initRadioButtons() {
        //settings male radio custom background on check
        binding.maleRadio.setOnClickListener {
            binding.maleRadioLayout.setBackgroundResource(R.drawable.radio_checked)
            binding.femaleRadioLayout.setBackgroundResource(R.drawable.radio_regular)
            binding.femaleRadio.isChecked = false
        }

        //settings female radio custom background on check
        binding.femaleRadio.setOnClickListener {
            binding.femaleRadioLayout.setBackgroundResource(R.drawable.radio_checked)
            binding.maleRadioLayout.setBackgroundResource(R.drawable.radio_regular)
            binding.maleRadio.isChecked = false
        }
    }

    /**
     * METHOD FOR INITIALIZING DATE OF BIRTH DIALOG AND GET DATE FROM IT
     */
    private fun initDOBDialog() {
        binding.dobEt.showSoftInputOnFocus = false
        binding.dobEt.onFocusChangeListener = View.OnFocusChangeListener { view, b ->
            if (b) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        binding.dobEt.setOnClickListener {

            val datePickerDialog = DatePickerDialog(this)
            datePickerDialog.setOnDateSetListener { _, year, month, dayOfMonth ->
                binding.dobEt.setText(
                    resources.getString(
                        R.string.edit_details_label_text_dob, year, month + 1, dayOfMonth
                    )
                )
            }
            datePickerDialog.show()
        }

    }

    /**
     * METHOD FOR CLEARING PROFILE IMAGE
     */
    private fun clearProfileImage() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Clear Profile Image")
            .setMessage("Are you sure you want to clear profile image?")
            .setPositiveButton("Yes") { _, _ ->
                binding.editDetailsIv.setImageResource(R.drawable.ic_profile)
                viewModel.profileImage = ""
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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
                selectedImageUri = UCrop.getOutput(it.data!!)!!

                //setting image to image view
                Glide.with(this).load(selectedImageUri).into(binding.editDetailsIv)
            }
        }

    /**
     *  METHOD TO GET PROFILE DATA FROM SERVER AND SET IT TO UI
     */
    private fun getDetails() {
        loadingDialog.show()
        viewModel.isDetailsLoaded.observe(this) {
            if (it != null) {
                //clearing progress dialog
                loadingDialog.dismiss()

                //setting data
                if (it == Constants.API_SUCCESS) {
                    binding.viewModel = viewModel

                    //setting image to imageview
                    Glide.with(this@EditDetailsActivity).load(viewModel.profileImage)
                        .placeholder(R.drawable.ic_profile).into(binding.editDetailsIv)

                    //setting gender radios
                    setRadioButtons(viewModel.gender)
                } else {
                    Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        viewModel.getDetails(this)
    }

    /**
     * METHOD FOR SETTING GENDER RADIO BUTTONS ON PROFILE LOAD
     */
    private fun setRadioButtons(radio: String = "M") {
        if (radio == "M") {
            binding.maleRadio.isChecked = true
            binding.maleRadioLayout.setBackgroundResource(R.drawable.radio_checked)
            binding.femaleRadioLayout.setBackgroundResource(R.drawable.radio_regular)
            binding.femaleRadio.isChecked = false
        } else {
            binding.femaleRadio.isChecked = true
            binding.femaleRadioLayout.setBackgroundResource(R.drawable.radio_checked)
            binding.maleRadioLayout.setBackgroundResource(R.drawable.radio_regular)
            binding.maleRadio.isChecked = false
        }
    }

    /**
     * METHOD FOR UPDATE PROFILE TO THE SERVER
     */
    private fun updateDetails() {
        val firstName = binding.profileFirstNameEt.text.toString().trim()
        val lastName = binding.profileLastNameEt.text.toString().trim()
        val gender = if (binding.maleRadio.isChecked) "M" else "F"
        val email = binding.profileEmailEt.text.toString().trim()
        val mobileNo = binding.profileMobileNoEt.text.toString().trim()
        val dob = binding.dobEt.text.toString().trim()
        val address = binding.profileAddressEt.text.toString().trim()

        //checking firstname empty field
        if (firstName.isEmpty()) {
            binding.profileFirstNameEt.error = "First name is required"
            binding.profileFirstNameEt.requestFocus()
            return
        } else {
            binding.profileFirstNameEt.error = null
        }

        //checking lastname empty field
        if (lastName.isEmpty()) {
            binding.profileLastNameEt.error = "Last name is required"
            binding.profileLastNameEt.requestFocus()
            return
        } else {
            binding.profileLastNameEt.error = null
        }

        //checking email empty field
        if (email.isEmpty()) {
            binding.profileEmailEt.error = "Email Required"
            binding.profileEmailEt.requestFocus()
            return
        } else {
            binding.profileEmailEt.error = null
        }

        //checking mobile no empty field
        if (mobileNo.isEmpty()) {
            binding.profileMobileNoEt.error = "Mobile no is required"
            binding.profileMobileNoEt.requestFocus()
            return
        } else {
            binding.profileMobileNoEt.error = null
        }

        //checking dob empty field
        if (dob.isEmpty()) {
            binding.dobEt.error = "Date of birth is required"
            binding.dobEt.requestFocus()
            return
        } else {
            binding.dobEt.error = null
        }

        //checking address empty field
        if (address.isEmpty()) {
            binding.profileAddressEt.error = "Address is required"
            binding.profileAddressEt.requestFocus()
            return
        } else {
            binding.profileAddressEt.error = null
        }

        loadingDialog.show()
        viewModel.isDetailsUpdated.observe(this) {
            if (it != null) {
                //clearing progress dialog
                loadingDialog.dismiss()

                //setting data
                if (it == Constants.API_SUCCESS) {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show()
                }
            }
        }

        //first image should be uploaded to server and then profile data
        uploadImage(object : ResponseCallback {
            override fun onSuccess(response: String) {
                viewModel.updateDetails(
                    this@EditDetailsActivity,
                    imageUrl = response,
                    firstName,
                    lastName,
                    gender,
                    email,
                    mobileNo,
                    dob,
                    address
                )
            }

            override fun onError(response: String) {
                loadingDialog.dismiss()
                Toast.makeText(
                    this@EditDetailsActivity, response, Toast.LENGTH_SHORT
                ).show()
            }
        })

    }

    /**
     * METHOD FOR UPLOADING IMAGE TO THE SERVER
     */
    private fun uploadImage(callback: ResponseCallback) {

        //if image is not changed then @selectedImageUri will be empty
        if (selectedImageUri.toString().isEmpty()) {
            callback.onSuccess(viewModel.profileImage)
            return
        }

        MediaManager.get().upload(selectedImageUri)
            .option("folder", "${viewModel.username}/")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    var imageUrl = resultData!!["url"].toString()
                    imageUrl = imageUrl.replace("http://", "https://")
                    callback.onSuccess(imageUrl)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    callback.onError("Error uploading image")
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}

            }).dispatch()
    }
}