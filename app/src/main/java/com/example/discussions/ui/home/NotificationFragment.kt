package com.example.discussions.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.discussions.R
import com.example.discussions.databinding.FragmentNotificationBinding
import com.example.discussions.viewModels.HomeViewModel

class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_notification, container, false)
        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        getAllNotifications()
        return binding.root
    }

    private fun getAllNotifications() {
        binding.notificationProgressBar.visibility = View.VISIBLE
        homeViewModel.notificationsList.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.notificationProgressBar.visibility = View.GONE
            }
        }

        homeViewModel.getAllNotifications(requireContext())
    }

}