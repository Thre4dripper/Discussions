package com.example.discussions.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.discussions.R

class ProfileRecyclerAdapter :
    RecyclerView.Adapter<ProfileRecyclerAdapter.ProfilePostsViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostsViewHolder {
        val view = View.inflate(parent.context, R.layout.item_user_post, null)
        return ProfilePostsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfilePostsViewHolder, position: Int) {
    }

    override fun getItemCount() = 10

    class ProfilePostsViewHolder(itemView: View) : ViewHolder(itemView) {
    }
}