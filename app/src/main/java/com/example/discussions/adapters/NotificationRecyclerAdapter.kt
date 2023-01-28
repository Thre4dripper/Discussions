package com.example.discussions.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.discussions.R
import com.example.discussions.databinding.ItemNotificationBinding
import com.example.discussions.models.NotificationModel
import java.text.SimpleDateFormat
import java.util.*

class NotificationRecyclerAdapter :
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
        (holder as NotificationViewHolder).bind(holder.binding, notification)
    }

    class NotificationViewHolder(itemView: View) : ViewHolder(itemView) {
        val binding = DataBindingUtil.bind<ItemNotificationBinding>(itemView)!!

        fun bind(binding: ItemNotificationBinding, notification: NotificationModel) {
            Glide.with(itemView.context)
                .load(notification.notifierImage)
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.itemNotificationUserImage)

            binding.itemNotificationContent.text = notification.notifierName

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