package com.example.discussions.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.discussions.adapters.PollsRecyclerAdapter
import com.example.discussions.databinding.FragmentPollsBinding
import com.example.discussions.viewModels.HomeViewModel

class PollsFragment : Fragment() {
    private val TAG = "PollsFragment"

    private lateinit var binding: FragmentPollsBinding
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPollsBinding.inflate(inflater, container, false)
        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        binding.pollsRv.adapter = PollsRecyclerAdapter()

        getAllUserPolls()
        return binding.root
    }

    private fun getAllUserPolls() {
        homeViewModel.userPollsList.observe(viewLifecycleOwner) {
            if (it != null) {
                Log.d(TAG, "getAllUserPolls: $it")
            }
        }

        homeViewModel.getAllUserPolls(requireContext())
    }
}