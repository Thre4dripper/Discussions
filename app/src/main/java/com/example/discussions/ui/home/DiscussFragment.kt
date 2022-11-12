package com.example.discussions.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
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
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }
        getAllPosts()
        return binding.root
    }

    private fun getAllPosts() {
        homeViewModel.postsList.observe(viewLifecycleOwner) {
            discussAdapter.submitList(it)
            Log.d(TAG, "getAllPosts: $it")
        }

        homeViewModel.getAllPosts(requireContext())
    }
}