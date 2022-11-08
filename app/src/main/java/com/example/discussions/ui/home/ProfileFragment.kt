package com.example.discussions.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.discussions.R
import com.example.discussions.databinding.FragmentProfileBinding
import com.example.discussions.databinding.LoadingDialogBinding
import com.example.discussions.ui.editDetails.EditDetailsActivity
import com.example.discussions.ui.settings.SettingsActivity
import com.example.discussions.viewModels.HomeViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: HomeViewModel

    private lateinit var loadingDialog: AlertDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        Glide.with(this)
            .load("https://media1.popsugar-assets.com/files/thumbor/8su-YMfzKmaONa2ODYaWx7dltHI/fit-in/500x500/filters:format_auto-!!-:strip_icc-!!-/2018/07/16/749/n/1922398/4c32bf875b4ccef29a4812.53698795_/i/Tom-Cruise.jpg")
            .into(binding.profileIv)

        binding.editProfileBtn.setOnClickListener {
            val intent = Intent(requireContext(), EditDetailsActivity::class.java)
            startActivity(intent)
        }

        binding.profileSettingsBtn.setOnClickListener {
            settingsCallback.launch(Intent(requireContext(), SettingsActivity::class.java))
        }

        initLoadingDialog()
        getProfile()
        return binding.root
    }

    private fun initLoadingDialog() {
        val dialogBinding = LoadingDialogBinding.inflate(layoutInflater)
        loadingDialog = MaterialAlertDialogBuilder(requireContext()).setView(dialogBinding.root)
            .setCancelable(false).show()
        loadingDialog.dismiss()
    }

    private fun getProfile() {
        loadingDialog.show()
        viewModel.isProfileLoaded.observe(viewLifecycleOwner) {
            if (it != null) {
                loadingDialog.dismiss()
                if (it == HomeViewModel.API_SUCCESS) {

                    viewModel.profileDataModel.profileImage =
                        viewModel.profileDataModel.profileImage.replace(
                            "http://",
                            "https://"
                        )

                    Glide.with(this)
                        .load(viewModel.profileDataModel.profileImage)
                        .into(binding.profileIv)

                    binding.profileNameTv.text = getString(
                        R.string.profile_name_text,
                        viewModel.profileDataModel.firstName,
                        viewModel.profileDataModel.lastName
                    )
                    binding.profileUsernameTv.text = viewModel.profileDataModel.username
                    binding.profilePostsCountTv.text = viewModel.profileDataModel.postsCount
                    binding.profilePollsCountTv.text = viewModel.profileDataModel.pollsCount
                } else {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Error")
                        .setMessage(it)
                        .setPositiveButton("Retry") { _, _ ->
                            getProfile()
                        }
                        .setNegativeButton("Cancel") { _, _ ->
                            requireActivity().finish()
                        }
                        .show()
                }

            }
        }

        viewModel.getProfile(requireContext())
    }

    /**
     * Settings Activity launch callback
     */
    private val settingsCallback =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == SettingsActivity.RESULT_LOGOUT) {
                requireActivity().setResult(SettingsActivity.RESULT_LOGOUT)
                requireActivity().finish()
            }
        }
}