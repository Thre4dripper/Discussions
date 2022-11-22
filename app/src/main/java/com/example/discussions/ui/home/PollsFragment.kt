package com.example.discussions.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.discussions.adapters.PollsRecyclerAdapter
import com.example.discussions.databinding.FragmentPollsBinding

class PollsFragment : Fragment() {
    private lateinit var binding: FragmentPollsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPollsBinding.inflate(inflater, container, false)

        binding.pollsRv.adapter = PollsRecyclerAdapter()
        return binding.root
    }
}