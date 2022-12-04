package com.example.discussions.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.example.discussions.Constants
import com.example.discussions.adapters.ProfileRecyclerAdapter
import com.example.discussions.adapters.interfaces.UserPostClickInterface
import com.example.discussions.databinding.FragmentProfileBinding
import com.example.discussions.databinding.LoadingDialogBinding
import com.example.discussions.ui.EditDetailsActivity
import com.example.discussions.ui.SettingsActivity
import com.example.discussions.ui.UserPostsActivity
import com.example.discussions.ui.ZoomImageActivity
import com.example.discussions.viewModels.HomeViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfileFragment : Fragment(), UserPostClickInterface {
    private val TAG = "ProfileFragment"

    private lateinit var binding: FragmentProfileBinding
    private lateinit var homeViewModel: HomeViewModel

    private lateinit var loadingDialog: AlertDialog
    private lateinit var retryDialog: AlertDialog
    private lateinit var profileAdapter: ProfileRecyclerAdapter

    private lateinit var settingsCallback: ActivityResultLauncher<Intent>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        //for launching profile image zoom activity
        binding.profileIv.setOnClickListener {
            val intent = Intent(requireContext(), ZoomImageActivity::class.java)
            intent.putExtra(Constants.ZOOM_IMAGE_URL, homeViewModel.profileDataModel.profileImage)
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

        binding.lifecycleOwner = this

        initDialogs()
        initPostsRecyclerView()
        initSettingsCallback()

        //swipe to refresh
        binding.profileSwipeLayout.setOnRefreshListener {
            homeViewModel.refreshProfile(requireContext())
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
        profileAdapter = ProfileRecyclerAdapter(this)
        binding.profilePostsRv.apply {
            adapter = profileAdapter
            layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    private fun getProfile() {
        loadingDialog.show()
        homeViewModel.isProfileFetched.observe(viewLifecycleOwner) {
            binding.profileSwipeLayout.isRefreshing = false
            if (it != null) {
                loadingDialog.dismiss()
                when (it) {
                    Constants.API_SUCCESS -> {

                        Glide.with(this)
                            .load(homeViewModel.profileDataModel.profileImage)
                            .into(binding.profileIv)

                        binding.profile = homeViewModel.profileDataModel

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

        homeViewModel.getProfile(requireContext())
    }

    private fun getUserPosts() {
        binding.profilePostsProgressBar.visibility = View.VISIBLE
        profileAdapter.submitList(mutableListOf())
        homeViewModel.userPostsList.observe(viewLifecycleOwner) {
            if (it != null) {
                //updating posts list recycler view
                profileAdapter.submitList(it)
                //updating posts count
                binding.profilePostsCountTv.text = it.size.toString()

                //hiding progress bar and lottie animation, when posts are loaded
                binding.profilePostsProgressBar.visibility = View.GONE
                binding.profilePostsLottieNoData.visibility = View.GONE

                //when no posts are available
                if (it.isEmpty()) {
                    //showing lottie animation
                    binding.profilePostsLottieNoData.visibility = View.VISIBLE
                    val error = homeViewModel.isUserPostsFetched.value

                    //when empty is due to network error, showing toast
                    if (homeViewModel.isUserPostsFetched.value != Constants.API_SUCCESS) {
                        Toast.makeText(
                            requireContext(),
                            homeViewModel.isUserPostsFetched.value,
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

        homeViewModel.getAllUserPosts(requireContext())
    }

    /**
     * Settings Activity launch callback
     */
    private fun initSettingsCallback() {
        settingsCallback =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Constants.RESULT_LOGOUT) {
                    requireActivity().setResult(Constants.RESULT_LOGOUT)
                    requireActivity().finish()
                }
            }
    }

    override fun onUserPostClick(index: Int) {
        val intent = Intent(requireContext(), UserPostsActivity::class.java)
        intent.putExtra(Constants.USER_POST_INDEX, index)
        intent.putExtra(Constants.USERNAME, binding.profileUsernameTv.text.toString())
        startActivity(intent)
    }
}