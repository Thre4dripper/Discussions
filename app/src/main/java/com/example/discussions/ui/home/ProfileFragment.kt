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
import com.example.discussions.Constants
import com.example.discussions.databinding.FragmentProfileBinding
import com.example.discussions.databinding.LoadingDialogBinding
import com.example.discussions.ui.editDetails.EditDetailsActivity
import com.example.discussions.ui.settings.SettingsActivity
import com.example.discussions.viewModels.HomeViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfileFragment : Fragment() {
    private val TAG = "ProfileFragment"

    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: HomeViewModel

    private lateinit var loadingDialog: AlertDialog
    private lateinit var retryDialog: AlertDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        binding.editProfileBtn.setOnClickListener {
            val intent = Intent(requireContext(), EditDetailsActivity::class.java)
            startActivity(intent)
        }

        binding.profileSettingsBtn.setOnClickListener {
            settingsCallback.launch(Intent(requireContext(), SettingsActivity::class.java))
        }

        binding.lifecycleOwner = this

        initDialogs()
        getProfile()
        return binding.root
    }

    private fun initDialogs() {
        val dialogBinding = LoadingDialogBinding.inflate(layoutInflater)
        loadingDialog = MaterialAlertDialogBuilder(requireContext()).setView(dialogBinding.root)
            .setCancelable(false).show()
        loadingDialog.dismiss()

        retryDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Oops!")
            .setMessage("Error getting profile")
            .setPositiveButton("Retry") { dialog, _ ->
                dialog.dismiss()
                getProfile()
            }
            .setNegativeButton("Cancel") { _, _ ->
                requireActivity().setResult(Constants.RESULT_CLOSE)
                requireActivity().finish()
            }
            .show()
    }

    private fun getProfile() {
        loadingDialog.show()
        viewModel.isApiFetched.observe(viewLifecycleOwner) {
            if (it != null) {
                loadingDialog.dismiss()
                if (it == Constants.API_SUCCESS) {

                    Glide.with(this)
                        .load(viewModel.profileDataModel.profileImage)
                        .into(binding.profileIv)

                    binding.profile = viewModel.profileDataModel
                } else {
                    retryDialog.show()
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
            if (it.resultCode == Constants.RESULT_LOGOUT) {
                requireActivity().setResult(Constants.RESULT_LOGOUT)
                requireActivity().finish()
            }
        }
}