package com.example.discussions.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.adapters.NotificationRecyclerAdapter
import com.example.discussions.databinding.FragmentNotificationBinding
import com.example.discussions.viewModels.HomeViewModel

class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding
    private lateinit var homeViewModel: HomeViewModel

    private lateinit var notificationAdapter: NotificationRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_notification, container, false)
        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        binding.notificationRv.apply {
            notificationAdapter = NotificationRecyclerAdapter()
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
            adapter = notificationAdapter
        }

        binding.notificationSwipeLayout.setOnRefreshListener {
            homeViewModel.refreshAllNotifications(requireContext())
        }

        getAllNotifications()
        return binding.root
    }

    private fun getAllNotifications() {
        binding.notificationProgressBar.visibility = View.VISIBLE
        homeViewModel.notificationsList.observe(viewLifecycleOwner) {
            if (it != null) {
                notificationAdapter.submitList(it) {
                    if (HomeViewModel.postsOrPollsOrNotificationsScrollToTop)
                        binding.notificationRv.scrollToPosition(0)
                }
                //hiding all loading
                binding.notificationSwipeLayout.isRefreshing = false
                binding.notificationProgressBar.visibility = View.GONE
                binding.notificationLottieNoData.visibility = View.GONE

                //when empty list is loaded
                if (it.isEmpty()) {
                    binding.notificationLottieNoData.visibility = View.VISIBLE
                    val error = homeViewModel.isNotificationsFetched.value

                    //when empty list is due to network error
                    if (error != Constants.API_SUCCESS) {
                        Toast.makeText(
                            requireContext(),
                            homeViewModel.isNotificationsFetched.value,
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

        homeViewModel.getAllNotifications(requireContext())
    }

}