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
import java.util.*

class NotificationRecyclerAdapter(private var notificationInterface: NotificationInterface) :
    ListAdapter<NotificationModel, ViewHolder>(
        NotificationDiffCallback()
    ) {

    companion object {
        const val NOTIFICATION_ITEM_TYPE_POST = 100
        const val NOTIFICATION_ITEM_TYPE_POLL = 101
        const val NOTIFICATION_ITEM_TYPE_COMMENT = 102
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        when (viewType) {
            NOTIFICATION_ITEM_TYPE_POST -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_notification_post, parent, false)
                return PostNotificationViewHolder(view)
            }
            NOTIFICATION_ITEM_TYPE_POLL -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_notification_poll, parent, false)
                return PollNotificationViewHolder(view)
            }
            NOTIFICATION_ITEM_TYPE_COMMENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_notification_comment, parent, false)
                return CommentNotificationViewHolder(view)
            }
            else -> return null!!
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

    override fun getItemViewType(position: Int): Int {
        return getItem(position).category
    }

    class PostNotificationViewHolder(itemView: View) : ViewHolder(itemView) {
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

            if (notifiedPost.postImage.isEmpty())
                binding.itemNotificationPostImage.visibility = View.GONE
            else
                binding.itemNotificationPostImage.visibility = View.VISIBLE

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