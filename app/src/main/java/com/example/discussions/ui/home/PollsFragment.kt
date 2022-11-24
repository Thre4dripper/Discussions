package com.example.discussions.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.discussions.Constants
import com.example.discussions.adapters.PollsRecyclerAdapter
import com.example.discussions.databinding.FragmentPollsBinding
import com.example.discussions.viewModels.HomeViewModel

class PollsFragment : Fragment() {
    private val TAG = "PollsFragment"

    private lateinit var binding: FragmentPollsBinding
    private lateinit var homeViewModel: HomeViewModel

    private lateinit var pollsAdapter: PollsRecyclerAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPollsBinding.inflate(inflater, container, false)
        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        binding.pollsRv.apply {
            pollsAdapter = PollsRecyclerAdapter()
            adapter = pollsAdapter
        }

        binding.pollsSwipeLayout.setOnRefreshListener {
            homeViewModel.refreshAllUserPolls(
                requireContext()
            )
        }

        getAllUserPolls()
        return binding.root
    }

    private fun getAllUserPolls() {
        binding.pollsProgressBar.visibility = View.VISIBLE
        homeViewModel.userPollsList.observe(viewLifecycleOwner) {
            if (it != null) {
                pollsAdapter.submitList(it) {
                    //scroll to top after loading new data
                    binding.pollsRv.scrollToPosition(0)
                }
                //hiding all loading
                binding.pollsSwipeLayout.isRefreshing = false
                binding.pollsProgressBar.visibility = View.GONE
                binding.pollsLottieNoData.visibility = View.GONE

                //when empty list is loaded
                if (it.isEmpty()) {
                    binding.pollsLottieNoData.visibility = View.VISIBLE
                    val error = homeViewModel.isUserPollsFetched.value

                    //when empty list is due to network error
                    if (error != Constants.API_SUCCESS) {
                        Toast.makeText(
                            requireContext(),
                            homeViewModel.isUserPollsFetched.value,
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

        homeViewModel.getAllUserPolls(requireContext())
    }
}