package com.example.discussions.ui.home

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.adapters.NotificationRecyclerAdapter
import com.example.discussions.adapters.interfaces.NotificationInterface
import com.example.discussions.databinding.FragmentNotificationBinding
import com.example.discussions.models.NotificationModel
import com.example.discussions.ui.PollDetailsActivity
import com.example.discussions.ui.PostDetailsActivity
import com.example.discussions.ui.bottomSheets.NotificationOptionsBS
import com.example.discussions.viewModels.HomeViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class NotificationFragment : Fragment(), NotificationInterface {
    private val TAG = "NotificationFragment"

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
            notificationAdapter = NotificationRecyclerAdapter(this@NotificationFragment)
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

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.notificationRv)

        setDeleteAllNotificationsButton()
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
                    if (error != Constants.API_SUCCESS && error != null) {
                        Toast.makeText(
                            requireContext(),
                            error,
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

    private fun setDeleteAllNotificationsButton() {
        homeViewModel.isAllNotificationsDeleted.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it == Constants.API_SUCCESS) {
                    Toast.makeText(
                        requireContext(),
                        "All Notifications Deleted",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error deleting notifications",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }

        binding.notificationClearAllBtn.setOnClickListener {
            if (homeViewModel.notificationsList.value == null || homeViewModel.notificationsList.value!!.isEmpty()) {
                Toast.makeText(requireContext(), "No notifications to clear", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            //if list is empty then no need to show dialog
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete All Notifications")
                .setMessage("Are you sure you want to delete all notifications?")
                .setPositiveButton("Yes") { _, _ ->
                    homeViewModel.deleteAllNotifications(requireContext())
                }
                .setNegativeButton("No") { _, _ -> }
                .setCancelable(false)
                .create()
                .show()
        }
    }


    private var swipeToDeleteCallback: ItemTouchHelper.SimpleCallback =
        object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                if (direction == ItemTouchHelper.LEFT) {
                    val notification = notificationAdapter.currentList[viewHolder.adapterPosition]
                    onNotificationMarkAsRead(notification.notificationId)
                } else {
                    val notification = notificationAdapter.currentList[viewHolder.adapterPosition]
                    onNotificationDelete(notification.notificationId)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )

                if (dX > 0) {
                    val icon = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_delete,
                        null
                    )
                    val background = RectF(
                        viewHolder.itemView.left.toFloat(),
                        viewHolder.itemView.top.toFloat(),
                        dX,
                        viewHolder.itemView.bottom.toFloat()
                    )
                    val iconMargin = (viewHolder.itemView.height - icon!!.intrinsicHeight) / 2
                    val iconTop =
                        viewHolder.itemView.top + (viewHolder.itemView.height - icon.intrinsicHeight) / 2
                    val iconBottom = iconTop + icon.intrinsicHeight
                    val iconLeft = viewHolder.itemView.left + iconMargin
                    val iconRight = viewHolder.itemView.left + iconMargin + icon.intrinsicWidth
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    icon.setTint(Color.WHITE)
                    val paint = Paint()
                    paint.color =
                        ResourcesCompat.getColor(resources, R.color.notification_delete_color, null)
                    c.drawRect(background, paint)
                    icon.draw(c)
                } else {
                    val icon = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_notification_read,
                        null
                    )
                    val background = RectF(
                        viewHolder.itemView.right.toFloat() + dX,
                        viewHolder.itemView.top.toFloat(),
                        viewHolder.itemView.right.toFloat(),
                        viewHolder.itemView.bottom.toFloat()
                    )
                    val iconMargin = (viewHolder.itemView.height - icon!!.intrinsicHeight) / 2
                    val iconTop =
                        viewHolder.itemView.top + (viewHolder.itemView.height - icon.intrinsicHeight) / 2
                    val iconBottom = iconTop + icon.intrinsicHeight
                    val iconLeft = viewHolder.itemView.right - iconMargin - icon.intrinsicWidth
                    val iconRight = viewHolder.itemView.right - iconMargin
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    icon.setTint(Color.WHITE)
                    val paint = Paint()
                    paint.color = ResourcesCompat.getColor(
                        resources,
                        R.color.notification_mark_as_read_color,
                        null
                    )
                    c.drawRect(background, paint)
                    icon.draw(c)
                }
            }
        }

    override fun onNotificationClick(notification: NotificationModel) {
        when (notification.category) {
            Constants.NOTIFICATION_CATEGORY_POST -> {
                val intent = Intent(requireContext(), PostDetailsActivity::class.java)
                val postId = notification.post!!.id
                intent.putExtra(Constants.POST_ID, postId)
                startActivity(intent)
                onNotificationMarkAsRead(notification.notificationId)
            }
            Constants.NOTIFICATION_CATEGORY_POLL -> {
                val intent = Intent(requireContext(), PollDetailsActivity::class.java)
                val pollId = notification.poll!!.id
                intent.putExtra(Constants.POLL_ID, pollId)
                startActivity(intent)
                onNotificationMarkAsRead(notification.notificationId)
            }
            Constants.NOTIFICATION_CATEGORY_COMMENT -> {
                //TODO open post or poll detail activity which has the comment
            }
        }
    }

    override fun onNotificationOptionsClick(notification: NotificationModel) {
        val notificationOptionsBs = NotificationOptionsBS(notification, this@NotificationFragment)
        notificationOptionsBs.show(
            requireActivity().supportFragmentManager,
            notificationOptionsBs.tag
        )
    }

    override fun onNotificationDelete(notificationId: String) {
        homeViewModel.isNotificationDeleted.observe(viewLifecycleOwner) {

            if (it != null && it != Constants.API_SUCCESS) {
                Toast.makeText(
                    requireContext(),
                    "Error deleting notification",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

        homeViewModel.deleteNotificationById(requireContext(), notificationId)
    }

    override fun onNotificationMarkAsRead(notificationId: String) {
        homeViewModel.isNotificationRead.observe(viewLifecycleOwner) {
            if (it != null && it != Constants.API_SUCCESS) {
                Toast.makeText(
                    requireContext(),
                    "Error Marking notification",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

        //manually update the notification item but only if it is already read
        val notification =
            notificationAdapter.currentList.find { it.notificationId == notificationId }!!
        if (notification.isRead) {
            val position = notificationAdapter.currentList.indexOf(notification)
            notificationAdapter.notifyItemChanged(position)
        }

        homeViewModel.readNotification(requireContext(), notificationId)
    }
}