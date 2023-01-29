package com.example.discussions.ui.bottomSheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.discussions.adapters.interfaces.NotificationInterface
import com.example.discussions.databinding.BsNotificationOptionsBinding
import com.example.discussions.models.NotificationModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NotificationOptionsBS(
    private var notification: NotificationModel,
    private var notificationInterface: NotificationInterface
) : BottomSheetDialogFragment() {
    private lateinit var binding: BsNotificationOptionsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BsNotificationOptionsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.optionDeleteNotificationTv.setOnClickListener {
            notificationInterface.onNotificationDelete(notification.notificationId)
            dismiss()
        }
        binding.optionMarkNotificationTv.setOnClickListener {
            notificationInterface.onNotificationMarkAsRead(notification.notificationId)
            dismiss()
        }
        binding.optionCancelCommentTv.setOnClickListener {
            dismiss()
        }
    }
}