package com.example.discussions.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.discussions.Constants
import com.example.discussions.adapters.DiscussionsRecyclerAdapter
import com.example.discussions.databinding.FragmentDiscussBinding
import com.example.discussions.viewModels.HomeViewModel

class DiscussFragment : Fragment() {
    private val TAG = "DiscussFragment"

    private lateinit var binding: FragmentDiscussBinding
    private lateinit var homeViewModel: HomeViewModel

    private lateinit var discussAdapter: DiscussionsRecyclerAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDiscussBinding.inflate(inflater, container, false)
        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        binding.discussionRv.apply {
            discussAdapter = DiscussionsRecyclerAdapter()
            adapter = discussAdapter
        }

        binding.swipeRefresh.setOnRefreshListener { getAllPosts() }
        getAllPosts()
        return binding.root
    }

    private fun getAllPosts() {
        binding.swipeRefresh.isRefreshing = true
        binding.discussLottieNoData.visibility = View.GONE
        homeViewModel.postsList.observe(viewLifecycleOwner) {
            if (it != null) {
                discussAdapter.submitList(it)
                binding.swipeRefresh.isRefreshing = false
                if (it.isEmpty()) {
                    binding.discussLottieNoData.visibility = View.VISIBLE
                    if (homeViewModel.isApiFetched.value != Constants.API_SUCCESS) {
                        Toast.makeText(
                            requireContext(),
                            homeViewModel.isApiFetched.value,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        }

        homeViewModel.getAllPosts(requireContext())
    }
}