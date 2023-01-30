package com.example.discussions.ui.bottomSheets

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.adapters.NotificationRecyclerAdapter
import com.example.discussions.adapters.interfaces.NotificationInterface
import com.example.discussions.databinding.BsNotificationOptionsBinding
import com.example.discussions.models.NotificationModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NotificationOptionsBS(
    private var notification: NotificationModel,
    private var notificationInterface: NotificationInterface
) : BottomSheetDialogFragment() {
    private val TAG = "NotificationOptionsBS"

    private lateinit var binding: BsNotificationOptionsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BsNotificationOptionsBinding.inflate(inflater, container, false)

        initNotificationDetails()
        return binding.root
    }

    private fun initNotificationDetails() {
        var notificationTypeCvBgColor = 0
        var notificationTypeIv = 0
        val notificationTitle = SpannableStringBuilder()
        val notificationContent = SpannableStringBuilder()

        //TODO bold notification content header
        when (notification.category) {
            NotificationRecyclerAdapter.NOTIFICATION_ITEM_TYPE_POST -> {

                notificationTypeCvBgColor = R.color.notification_post_item_bg_color
                notificationTypeIv = R.drawable.ic_image

                val notifiedPost = notification.post!!
                if (notification.type == Constants.NOTIFICATION_TYPE_LIKE) {
                    notificationTitle.append(
                        HtmlCompat.fromHtml(
                            "<b>${notification.notifierName}</b> Liked your post",
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )

                    notificationContent.append(
                        HtmlCompat.fromHtml(
                            "<b>Post:</b> ${(notifiedPost.title.ifEmpty { notifiedPost.content })}",
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )
                    binding.optionNotificationContent.apply {
                        text = notificationContent
                        visibility =
                            if (notifiedPost.title.isEmpty() && notifiedPost.content.isEmpty()) View.GONE else View.VISIBLE
                    }
                } else {
                    notificationTitle.append(
                        HtmlCompat.fromHtml(
                            "<b>${notification.notifierName}</b> Commented on your post",
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )

                    notificationContent.append(
                        HtmlCompat.fromHtml(
                            "<b>Comment:</b> ${notifiedPost.postComment}",
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )
                    binding.optionNotificationContent.apply {
                        text = notificationContent
                        visibility = View.VISIBLE
                    }
                }

                binding.optionNotificationTitle.text = notificationTitle
            }
            NotificationRecyclerAdapter.NOTIFICATION_ITEM_TYPE_POLL -> {
                notificationTypeCvBgColor = R.color.notification_poll_item_bg_color
                notificationTypeIv = R.drawable.create_poll_shortcut

                val notifiedPoll = notification.poll!!
                if (notification.type == Constants.NOTIFICATION_TYPE_LIKE) {
                    notificationTitle.append(
                        HtmlCompat.fromHtml(
                            "<b>${notification.notifierName}</b> Liked your poll",
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )

                    notificationContent.append(
                        HtmlCompat.fromHtml(
                            "<b>Poll:</b> ${(notifiedPoll.title.ifEmpty { notifiedPoll.content })}",
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )
                    binding.optionNotificationContent.apply {
                        text = notificationContent
                        visibility =
                            if (notifiedPoll.title.isEmpty() && notifiedPoll.content.isEmpty()) View.GONE else View.VISIBLE
                    }
                } else {
                    notificationTitle.append(
                        HtmlCompat.fromHtml(
                            "<b>${notification.notifierName}</b> Commented on your poll",
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )

                    notificationContent.append(
                        HtmlCompat.fromHtml(
                            "<b>Comment:</b> ${notifiedPoll.pollComment}",
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )
                    binding.optionNotificationContent.apply {
                        text = notificationContent
                        visibility = View.VISIBLE
                    }
                }
            }
            NotificationRecyclerAdapter.NOTIFICATION_ITEM_TYPE_COMMENT -> {
                notificationTypeCvBgColor = R.color.notification_comment_item_bg_color
                notificationTypeIv = R.drawable.ic_faq

                val notifiedComment = notification.comment!!
                if (notification.type == Constants.NOTIFICATION_TYPE_LIKE) {
                    notificationTitle.append(
                        HtmlCompat.fromHtml(
                            "<b>${notification.notifierName}</b> Liked your comment",
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )

                    notificationContent.append(
                        HtmlCompat.fromHtml(
                            "<b>Comment:</b> ${notifiedComment.content}",
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )
                    binding.optionNotificationContent.text = notificationContent
                } else {
                    notificationTitle.append(
                        HtmlCompat.fromHtml(
                            "<b>${notification.notifierName}</b> Replied on your comment",
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )

                    notificationContent.append(
                        HtmlCompat.fromHtml(
                            "<b>Comment:</b> ${notifiedComment.content}",
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )
                    binding.optionNotificationContent.text = notificationContent
                }
            }
        }

        // Set notification image
        Glide.with(requireContext())
            .load(notification.notifierImage)
            .placeholder(R.drawable.ic_profile)
            .circleCrop()
            .into(binding.optionNotificationIv)

        binding.optionNotificationTypeCv.setCardBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                notificationTypeCvBgColor,
                null
            )
        )

        binding.optionNotificationTypeIv.setImageResource(notificationTypeIv)
        binding.optionNotificationTitle.text = notificationTitle
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