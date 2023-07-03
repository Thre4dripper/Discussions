package com.example.discussions.adapters

import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.adapters.interfaces.NotificationInterface
import com.example.discussions.databinding.ItemNotificationCommentBinding
import com.example.discussions.databinding.ItemNotificationPollBinding
import com.example.discussions.databinding.ItemNotificationPostBinding
import com.example.discussions.models.NotificationModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class NotificationRecyclerAdapter(private var notificationInterface: NotificationInterface) :
    ListAdapter<NotificationModel, ViewHolder>(
        NotificationDiffCallback()
    ) {

    companion object {
        const val NOTIFICATION_ITEM_TYPE_POST = Constants.VIEW_TYPE_POST
        const val NOTIFICATION_ITEM_TYPE_POLL = Constants.VIEW_TYPE_POLL
        const val NOTIFICATION_ITEM_TYPE_COMMENT = Constants.VIEW_TYPE_COMMENT
        const val NOTIFICATION_ITEM_TYPE_LOADING = Constants.VIEW_TYPE_LOADING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            NOTIFICATION_ITEM_TYPE_POST -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_notification_post, parent, false)
                PostNotificationViewHolder(view)
            }

            NOTIFICATION_ITEM_TYPE_POLL -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_notification_poll, parent, false)
                PollNotificationViewHolder(view)
            }

            NOTIFICATION_ITEM_TYPE_COMMENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_notification_comment, parent, false)
                CommentNotificationViewHolder(view)
            }

            NOTIFICATION_ITEM_TYPE_LOADING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.pagination_loader, parent, false)
                LoadingViewHolder(view)
            }

            else -> null!!
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = getItem(position)

        when (holder.itemViewType) {
            NOTIFICATION_ITEM_TYPE_POST -> {
                (holder as PostNotificationViewHolder).bind(
                    holder.binding,
                    notification,
                    notificationInterface
                )

                holder.binding.root.animation = AnimationUtils.loadAnimation(
                    holder.binding.root.context,
                    R.anim.translate
                )
            }

            NOTIFICATION_ITEM_TYPE_POLL -> {
                (holder as PollNotificationViewHolder).bind(
                    holder.binding,
                    notification,
                    notificationInterface
                )

                holder.binding.root.animation = AnimationUtils.loadAnimation(
                    holder.binding.root.context,
                    R.anim.translate
                )
            }

            NOTIFICATION_ITEM_TYPE_COMMENT -> {
                (holder as CommentNotificationViewHolder).bind(
                    holder.binding,
                    notification,
                    notificationInterface
                )

                holder.binding.root.animation = AnimationUtils.loadAnimation(
                    holder.binding.root.context,
                    R.anim.translate
                )
            }
        }
    }

    override fun submitList(list: MutableList<NotificationModel>?) {
        afterSubmitList(list)
        super.submitList(list)
    }

    override fun submitList(list: MutableList<NotificationModel>?, commitCallback: Runnable?) {
        afterSubmitList(list)
        super.submitList(list, commitCallback)
    }

    private fun afterSubmitList(list: MutableList<NotificationModel>?) {
        list?.removeIf { it.category == Constants.NOTIFICATION_CATEGORY_LOADER }

        if (list?.size != 0 && list?.last()?.next != null) {
            list.add(
                NotificationModel(
                    "",
                    0,
                    "",
                    "",
                    "",
                    Constants.NOTIFICATION_CATEGORY_LOADER,
                    false,
                    "",
                    "",
                    "",
                    null,
                    null,
                    null,
                )
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        val notification = getItem(position)
        return when (notification.category) {
            Constants.NOTIFICATION_CATEGORY_POST -> NOTIFICATION_ITEM_TYPE_POST
            Constants.NOTIFICATION_CATEGORY_POLL -> NOTIFICATION_ITEM_TYPE_POLL
            Constants.NOTIFICATION_CATEGORY_COMMENT -> NOTIFICATION_ITEM_TYPE_COMMENT
            Constants.NOTIFICATION_CATEGORY_LOADER -> NOTIFICATION_ITEM_TYPE_LOADING
            else -> null!!
        }
    }

    class PostNotificationViewHolder(itemView: View) : ViewHolder(itemView) {
        private val TAG = "PostNotificationViewHolder"
        val binding = DataBindingUtil.bind<ItemNotificationPostBinding>(itemView)!!

        fun bind(
            binding: ItemNotificationPostBinding,
            notification: NotificationModel,
            notificationInterface: NotificationInterface
        ) {
            Glide.with(itemView.context)
                .load(notification.notifierImage)
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.itemNotificationUserImage)

            binding.itemNotification.apply {
                //setting on click on the whole notification
                setOnClickListener { notificationInterface.onNotificationClick(notification) }
                //setting on long click on the whole notification
                setOnLongClickListener {
                    notificationInterface.onNotificationOptionsClick(notification)
                    true
                }
                //setting background color based on notification read status
                setBackgroundColor(
                    if (notification.isRead) itemView.context.getColor(R.color.white)
                    else itemView.context.getColor(R.color.notification_bg_color)
                )
            }

            //setting on click on the notification options
            binding.itemNotificationOptions.setOnClickListener {
                notificationInterface.onNotificationOptionsClick(notification)
            }

            val notificationText = SpannableStringBuilder()
            val notifiedPost = notification.post!!
            if (notification.type == Constants.NOTIFICATION_TYPE_LIKE) {
                notificationText.append(
                    HtmlCompat.fromHtml(
                        "<b>${notification.notifierName} </b>liked your post:",
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                )
                binding.itemNotificationTitle.text = notificationText
                binding.itemNotificationPostContent.apply {
                    text = if (notifiedPost.title.isNotEmpty()) {
                        String.format("%s", notifiedPost.title)
                    } else {
                        String.format("%s", notifiedPost.content)
                    }
                    visibility =
                        if (notifiedPost.title.isEmpty() && notifiedPost.content.isEmpty()) View.GONE else View.VISIBLE
                    textSize = 15f
                }
            } else {
                notificationText.append(
                    HtmlCompat.fromHtml(
                        "<b>${notification.notifierName} </b>commented on your post:",
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                )
                binding.itemNotificationTitle.text = notificationText
                binding.itemNotificationPostContent.apply {
                    text = notifiedPost.postComment
                    visibility = View.VISIBLE
                    textSize = 16f
                }
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = dateFormat.parse(notification.createdAt)

            binding.itemNotificationTime.text = DateUtils.getRelativeTimeSpanString(
                date!!.time,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )

            binding.itemNotificationPostImageCv.visibility =
                if (notifiedPost.postImage.isEmpty()) View.GONE else View.VISIBLE

            Glide.with(itemView.context)
                .load(notifiedPost.postImage)
                .into(binding.itemNotificationPostImage)
        }
    }

    class PollNotificationViewHolder(itemView: View) : ViewHolder(itemView) {
        val binding = DataBindingUtil.bind<ItemNotificationPollBinding>(itemView)!!
        fun bind(
            binding: Any,
            notification: NotificationModel,
            notificationInterface: NotificationInterface
        ) {
            binding as ItemNotificationPollBinding
            Glide.with(itemView.context)
                .load(notification.notifierImage)
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.itemNotificationUserImage)

            binding.itemNotification.apply {
                //setting on click on the whole notification
                setOnClickListener { notificationInterface.onNotificationClick(notification) }
                //setting on long click on the whole notification
                setOnLongClickListener {
                    notificationInterface.onNotificationOptionsClick(notification)
                    true
                }
                //setting background color based on notification read status
                setBackgroundColor(
                    if (notification.isRead) itemView.context.getColor(R.color.white)
                    else itemView.context.getColor(R.color.notification_bg_color)
                )
            }

            //setting on click on the notification options
            binding.itemNotificationOptions.setOnClickListener {
                notificationInterface.onNotificationOptionsClick(notification)
            }

            val notificationText = SpannableStringBuilder()
            val notifiedPoll = notification.poll!!
            if (notification.type == Constants.NOTIFICATION_TYPE_LIKE) {
                notificationText.append(
                    HtmlCompat.fromHtml(
                        "<b>${notification.notifierName} </b>liked your poll:",
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                )
                binding.itemNotificationTitle.text = notificationText
                binding.itemNotificationPollContent.apply {
                    text = if (notifiedPoll.title.isNotEmpty()) {
                        String.format("%s", notifiedPoll.title)
                    } else {
                        String.format("%s", notifiedPoll.content)
                    }
                    visibility =
                        if (notifiedPoll.title.isEmpty() && notifiedPoll.content.isEmpty()) View.GONE else View.VISIBLE
                    textSize = 15f
                }
            } else {
                notificationText.append(
                    HtmlCompat.fromHtml(
                        "<b>${notification.notifierName} </b>commented on your post:",
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                )
                binding.itemNotificationTitle.text = notificationText
                binding.itemNotificationPollContent.apply {
                    text = notifiedPoll.pollComment
                    visibility = View.VISIBLE
                    textSize = 16f
                }
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = dateFormat.parse(notification.createdAt)

            binding.itemNotificationTime.text = DateUtils.getRelativeTimeSpanString(
                date!!.time,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
        }
    }

    class CommentNotificationViewHolder(itemView: View) : ViewHolder(itemView) {
        val binding = DataBindingUtil.bind<ItemNotificationCommentBinding>(itemView)!!
        fun bind(
            binding: ItemNotificationCommentBinding,
            notification: NotificationModel,
            notificationInterface: NotificationInterface
        ) {
            Glide.with(itemView.context)
                .load(notification.notifierImage)
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.itemNotificationUserImage)

            binding.itemNotification.apply {
                //setting on click on the whole notification
                setOnClickListener { notificationInterface.onNotificationClick(notification) }
                //setting on long click on the whole notification
                setOnLongClickListener {
                    notificationInterface.onNotificationOptionsClick(notification)
                    true
                }
                //setting background color based on notification read status
                setBackgroundColor(
                    if (notification.isRead) itemView.context.getColor(R.color.white)
                    else itemView.context.getColor(R.color.notification_bg_color)
                )
            }

            //setting on click on the notification options
            binding.itemNotificationOptions.setOnClickListener {
                notificationInterface.onNotificationOptionsClick(notification)
            }

            val notificationText = SpannableStringBuilder()
            val notifiedComment = notification.comment!!
            if (notification.type == Constants.NOTIFICATION_TYPE_LIKE) {
                notificationText.append(
                    HtmlCompat.fromHtml(
                        "<b>${notification.notifierName} </b>liked your comment:",
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                )
                binding.itemNotificationTitle.text = notificationText
                binding.itemNotificationComment.text = notifiedComment.content
                binding.itemNotificationCommentContent.apply { visibility = View.GONE }
            } else {
                notificationText.append(
                    HtmlCompat.fromHtml(
                        "<b>${notification.notifierName} </b>replied your comment:",
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                )
                binding.itemNotificationTitle.text = notificationText
                binding.itemNotificationComment.text = notifiedComment.content
                binding.itemNotificationCommentContent.apply {
                    text = notifiedComment.comment
                    visibility =
                        if (notifiedComment.comment!!.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    inner class LoadingViewHolder(itemView: View) : ViewHolder(itemView)

    class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationModel>() {
        override fun areItemsTheSame(
            oldItem: NotificationModel,
            newItem: NotificationModel,
        ) = oldItem.notificationId == newItem.notificationId


        override fun areContentsTheSame(
            oldItem: NotificationModel,
            newItem: NotificationModel,
        ) = oldItem == newItem
    }

}