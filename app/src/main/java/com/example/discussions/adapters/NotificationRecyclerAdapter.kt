package com.example.discussions.adapters

import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.adapters.interfaces.NotificationInterface
import com.example.discussions.databinding.ItemNotificationBinding
import com.example.discussions.models.NotificationModel
import java.text.SimpleDateFormat
import java.util.*

class NotificationRecyclerAdapter(private var notificationInterface: NotificationInterface) :
    ListAdapter<NotificationModel, ViewHolder>(
        NotificationDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = getItem(position)
        (holder as NotificationViewHolder).bind(holder.binding, notification, notificationInterface)
    }

    class NotificationViewHolder(itemView: View) : ViewHolder(itemView) {
        val binding = DataBindingUtil.bind<ItemNotificationBinding>(itemView)!!

        fun bind(
            binding: ItemNotificationBinding,
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

            val category = if (notification.post != null) Constants.NOTIFICATION_CATEGORY_POST
            else if (notification.poll != null) Constants.NOTIFICATION_CATEGORY_POLL
            else Constants.NOTIFICATION_CATEGORY_COMMENT

            //TODO subject to change
            val notificationText = SpannableStringBuilder()
            when (category) {
                Constants.NOTIFICATION_CATEGORY_POST -> {
                    if (notification.type == Constants.NOTIFICATION_TYPE_LIKE) {
                        notificationText.append(
                            HtmlCompat.fromHtml(
                                "<b>${notification.notifierName} </b>liked your post:" +
                                        "<br><b>${notification.post!!.title}</b> <br>${notification.post.content}",
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            )
                        )
                        binding.itemNotificationContent.text = notificationText
                    } else {
                        notificationText.append(
                            HtmlCompat.fromHtml(
                                "<b>${notification.notifierName} </b>commented your post:" +
                                        "<br><b>${notification.post!!.title} ${notification.post.content} </b><br><b>${notification.post.postComment}</b>",
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            )
                        )
                        binding.itemNotificationContent.text = notificationText
                    }
                }
                Constants.NOTIFICATION_CATEGORY_POLL -> {
                    if (notification.type == Constants.NOTIFICATION_TYPE_LIKE) {
                        notificationText.append(
                            HtmlCompat.fromHtml(
                                "<b>${notification.notifierName} </b>liked your poll:" +
                                        "<br><b>${notification.poll!!.title}</b> <br>${notification.poll.content}",
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            )
                        )
                        binding.itemNotificationContent.text = notificationText
                    } else {
                        notificationText.append(
                            HtmlCompat.fromHtml(
                                "<b>${notification.notifierName} </b>commented your poll:" +
                                        "<br><b>${notification.poll!!.title} ${notification.poll.content} </b><br><b>${notification.poll.pollComment}</b>",
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            )
                        )
                        binding.itemNotificationContent.text = notificationText
                    }
                }
                Constants.NOTIFICATION_CATEGORY_COMMENT -> {
                    if (notification.type == Constants.NOTIFICATION_TYPE_LIKE) {
                        notificationText.append(
                            HtmlCompat.fromHtml(
                                "<b>${notification.notifierName} </b>liked your comment:" +
                                        "<br><b>${notification.comment!!.comment}</b>",
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            )
                        )
                        binding.itemNotificationContent.text = notificationText
                    } else {
                        notificationText.append(
                            HtmlCompat.fromHtml(
                                "<b>${notification.notifierName} </b>replied your comment:" +
                                        "<br><b>${notification.comment!!.comment}</b>",
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            )
                        )
                        binding.itemNotificationContent.text = notificationText
                    }
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