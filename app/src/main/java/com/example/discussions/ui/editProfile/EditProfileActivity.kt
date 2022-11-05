package com.example.discussions.ui.editProfile

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.discussions.R
import com.example.discussions.databinding.ActivityEditProfileBinding
import com.example.discussions.viewModels.EditProfileViewModel

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var viewModel: EditProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile)
        viewModel = ViewModelProvider(this)[EditProfileViewModel::class.java]

        binding.editProfileBackBtn.setOnClickListener {
            finish()
        }

        binding.lifecycleOwner = this
        getProfile()

        initRadioButtons()
        initDOBDialog()
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
                        R.string.edit_profile_label_text_dob, year, month + 1, dayOfMonth
                    )
                )
            }
            datePickerDialog.show()
        }

    }

    private fun getProfile() {
        viewModel.isProfileLoaded.observe(this) {
            if (it != null) {
                //clearing progress dialog
//                progressDialog.dismiss()

                //setting data
                if (it) {
                    binding.viewModel = viewModel
                    //setting gender radios
//                    setRadioButtons(viewModel.gender)
                } else {
                    Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        viewModel.getProfile(this)
    }
}