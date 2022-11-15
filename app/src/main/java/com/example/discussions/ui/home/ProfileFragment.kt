package com.example.discussions.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.example.discussions.Constants
import com.example.discussions.adapters.ProfileRecyclerAdapter
import com.example.discussions.databinding.FragmentProfileBinding
import com.example.discussions.databinding.LoadingDialogBinding
import com.example.discussions.ui.EditDetailsActivity
import com.example.discussions.ui.SettingsActivity
import com.example.discussions.ui.UserPostsActivity
import com.example.discussions.ui.ZoomImageActivity
import com.example.discussions.viewModels.HomeViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfileFragment : Fragment() {
    private val TAG = "ProfileFragment"

    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: HomeViewModel

    private lateinit var loadingDialog: AlertDialog
    private lateinit var retryDialog: AlertDialog
    private lateinit var profileAdapter: ProfileRecyclerAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        //for launching profile image zoom activity
        binding.profileIv.setOnClickListener {
            val intent = Intent(requireContext(), ZoomImageActivity::class.java)
            intent.putExtra(Constants.ZOOM_IMAGE_URL, viewModel.profileDataModel.profileImage)
            startActivity(intent)
        }

        //for launching edit details activity
        binding.editProfileBtn.setOnClickListener {
            val intent = Intent(requireContext(), EditDetailsActivity::class.java)
            startActivity(intent)
        }

        //for launching settings activity
        binding.profileSettingsBtn.setOnClickListener {
            settingsCallback.launch(Intent(requireContext(), SettingsActivity::class.java))
        }

        //for launching user posts activity
        binding.profilePostsLabelCv.setOnClickListener {
            userPostsCallback.launch(Intent(requireContext(), UserPostsActivity::class.java))
        }

        binding.lifecycleOwner = this

        initDialogs()
        initPostsRecyclerView()

        //swipe to refresh
        binding.profileSwipeLayout.setOnRefreshListener {
            viewModel.refreshProfile(requireContext())
            getProfile()
        }

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
        retryDialog.dismiss()
    }

    private fun initPostsRecyclerView() {
        profileAdapter = ProfileRecyclerAdapter()
        binding.profilePostsRv.adapter = profileAdapter
        binding.profilePostsRv.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    private fun getProfile() {
        loadingDialog.show()
        viewModel.isProfileFetched.observe(viewLifecycleOwner) {
            binding.profileSwipeLayout.isRefreshing = false
            if (it != null) {
                loadingDialog.dismiss()
                when (it) {
                    Constants.API_SUCCESS -> {

                        Glide.with(this)
                            .load(viewModel.profileDataModel.profileImage)
                            .into(binding.profileIv)

                        binding.profile = viewModel.profileDataModel

                        getUserPosts()
                    }
                    Constants.AUTH_FAILURE_ERROR -> {
                        requireActivity().setResult(Constants.RESULT_LOGOUT)
                        requireActivity().finish()
                    }
                    else -> {
                        retryDialog.show()
                    }
                }

            }
        }

        viewModel.getProfile(requireContext())
    }

    private fun getUserPosts() {
        binding.profilePostsProgressBar.visibility = View.VISIBLE
        profileAdapter.submitList(mutableListOf())
        viewModel.userPostsList.observe(viewLifecycleOwner) {
            if (it != null) {
                profileAdapter.submitList(it)
                binding.profilePostsProgressBar.visibility = View.GONE
                binding.profilePostsLottieNoData.visibility = View.GONE
                if (it.isEmpty()) {
                    binding.profilePostsLottieNoData.visibility = View.VISIBLE
                    val error = viewModel.isUserPostsFetched.value
                    if (viewModel.isUserPostsFetched.value != Constants.API_SUCCESS) {
                        Toast.makeText(
                            requireContext(),
                            viewModel.isUserPostsFetched.value,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    if (error == Constants.AUTH_FAILURE_ERROR) {
                        requireActivity().setResult(Constants.RESULT_LOGOUT)
                        requireActivity().finish()
                    }
                }
            }
        }

        viewModel.getAllUserPosts(requireContext())
    }

    private val userPostsCallback = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        //TODO update user posts list in recycler view without refreshing the whole fragment
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