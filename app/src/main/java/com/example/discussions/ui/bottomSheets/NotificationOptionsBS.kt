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
import com.example.discussions.models.CommentNotificationModel
import com.example.discussions.models.NotificationModel
import com.example.discussions.models.PollNotificationModel
import com.example.discussions.models.PostNotificationModel
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

        when (notification.category) {
            NotificationRecyclerAdapter.NOTIFICATION_ITEM_TYPE_POST -> {

                notificationTypeCvBgColor = R.color.notification_post_item_bg_color
                notificationTypeIv = R.drawable.ic_image

                setPostNotificationDetails(notificationTitle)

                binding.optionNotificationTitle.text = notificationTitle
            }
            NotificationRecyclerAdapter.NOTIFICATION_ITEM_TYPE_POLL -> {
                notificationTypeCvBgColor = R.color.notification_poll_item_bg_color
                notificationTypeIv = R.drawable.create_poll_shortcut

                setPollNotificationDetails(notificationTitle)
            }
            NotificationRecyclerAdapter.NOTIFICATION_ITEM_TYPE_COMMENT -> {
                notificationTypeCvBgColor = R.color.notification_comment_item_bg_color
                notificationTypeIv = R.drawable.ic_faq

                setCommentNotificationDetails(notificationTitle)
            }
        }

        // Set notification image
        Glide.with(requireContext())
            .load(notification.notifierImage)
            .placeholder(R.drawable.ic_profile)
            .circleCrop()
            .into(binding.optionNotificationIv)

        // set notification small icon background color
        binding.optionNotificationTypeCv.setCardBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                notificationTypeCvBgColor,
                null
            )
        )

        //set notification small icon
        binding.optionNotificationTypeIv.setImageResource(notificationTypeIv)
        binding.optionNotificationTitle.text = notificationTitle
    }

    /**
     * SET NOTIFICATION DETAILS FOR POST
     */
    private fun setPostNotificationDetails(notificationTitle: SpannableStringBuilder) {
        val notifiedPost = notification.post!!
        if (notification.type == Constants.NOTIFICATION_TYPE_LIKE) {
            setPostLikeContent(notificationTitle, notifiedPost)
        } else {
            setPostCommentContent(notificationTitle, notifiedPost)
        }
    }

    /**
     * SET NOTIFICATION DETAILS FOR POLL
     */
    private fun setPollNotificationDetails(notificationTitle: SpannableStringBuilder) {
        val notifiedPoll = notification.poll!!
        if (notification.type == Constants.NOTIFICATION_TYPE_LIKE) {
            setPollLikeContent(notificationTitle, notifiedPoll)
        } else {
            setPollCommentContent(notificationTitle, notifiedPoll)
        }
    }

    /**
     * SET NOTIFICATION DETAILS FOR COMMENT
     */
    private fun setCommentNotificationDetails(notificationTitle: SpannableStringBuilder) {
        val notifiedComment = notification.comment!!
        if (notification.type == Constants.NOTIFICATION_TYPE_LIKE) {
            setCommentLikeContent(notificationTitle, notifiedComment)
        } else {
            setCommentReplyContent(notificationTitle, notifiedComment)
        }
    }

    /**
     * METHODS TO SET NOTIFICATION CONTENT FOR POST LIKE AND COMMENT
     */
    private fun setPostLikeContent(
        notificationTitle: SpannableStringBuilder,
        notifiedPost: PostNotificationModel
    ) {
        // set post notification title for like notification
        notificationTitle.append(
            HtmlCompat.fromHtml(
                "<b>${notification.notifierName}</b> Liked your post",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        )

        // set post notification content header with visibility control
        binding.optionNotificationContentHeader.apply {
            text = getString(R.string.notification_options_content_header, "Post")
            visibility =
                if (notifiedPost.title.isEmpty() && notifiedPost.content.isEmpty()) View.GONE else View.VISIBLE
        }

        // set post notification content with visibility control
        binding.optionNotificationContent.apply {
            text = notifiedPost.title.ifEmpty { notifiedPost.content }
            visibility =
                if (notifiedPost.title.isEmpty() && notifiedPost.content.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun setPostCommentContent(
        notificationTitle: SpannableStringBuilder,
        notifiedPost: PostNotificationModel
    ) {
        // set post notification title for comment notification
        notificationTitle.append(
            HtmlCompat.fromHtml(
                "<b>${notification.notifierName}</b> Commented on your post",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        )

        // set post notification content header
        binding.optionNotificationContentHeader.text =
            getString(R.string.notification_options_content_header, "Comment")

        // set post notification content
        binding.optionNotificationContent.apply {
            text = notifiedPost.postComment
            visibility = View.VISIBLE
        }
    }

    /**
     * METHODS TO SET NOTIFICATION CONTENT FOR COMMENT LIKE AND REPLY
     */
    private fun setPollLikeContent(
        notificationTitle: SpannableStringBuilder,
        notifiedPoll: PollNotificationModel
    ) {
        // set poll notification title for like notification
        notificationTitle.append(
            HtmlCompat.fromHtml(
                "<b>${notification.notifierName}</b> Liked your poll",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        )

        // set poll notification content header with visibility control
        binding.optionNotificationContentHeader.text =
            getString(R.string.notification_options_content_header, "Poll")

        // set poll notification content with visibility control
        binding.optionNotificationContent.apply {
            text = notifiedPoll.title.ifEmpty { notifiedPoll.content }
            visibility =
                if (notifiedPoll.title.isEmpty() && notifiedPoll.content.isEmpty()) View.GONE else View.VISIBLE
        }
    }


    private fun setPollCommentContent(
        notificationTitle: SpannableStringBuilder,
        notifiedPoll: PollNotificationModel
    ) {
        // set poll notification title for comment notification
        notificationTitle.append(
            HtmlCompat.fromHtml(
                "<b>${notification.notifierName}</b> Commented on your poll",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        )

        // set poll notification content header
        binding.optionNotificationContentHeader.text =
            getString(R.string.notification_options_content_header, "Comment")

        // set poll notification content
        binding.optionNotificationContent.apply {
            text = notifiedPoll.pollComment
            visibility = View.VISIBLE
        }
    }

    /**
     * METHODS TO SET NOTIFICATION CONTENT FOR COMMENT LIKE AND REPLY
     */
    private fun setCommentLikeContent(
        notificationTitle: SpannableStringBuilder,
        notifiedComment: CommentNotificationModel
    ) {
        // set comment notification title for like notification
        notificationTitle.append(
            HtmlCompat.fromHtml(
                "<b>${notification.notifierName}</b> Liked your comment",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        )

        // set comment notification content header
        binding.optionNotificationContentHeader.text =
            getString(R.string.notification_options_content_header, "Comment")

        // set comment notification content
        binding.optionNotificationContent.text = notifiedComment.content
    }

    private fun setCommentReplyContent(
        notificationTitle: SpannableStringBuilder,
        notifiedComment: CommentNotificationModel
    ) {
        // set comment notification title for reply notification
        notificationTitle.append(
            HtmlCompat.fromHtml(
                "<b>${notification.notifierName}</b> Replied on your comment",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        )

        // set comment notification content header
        binding.optionNotificationContentHeader.text =
            getString(R.string.notification_options_content_header, "Reply")

        // set comment notification content
        binding.optionNotificationContent.text = notifiedComment.content
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