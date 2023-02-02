package com.example.discussions.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.discussions.R
import com.example.discussions.databinding.ActivityPostDetailsBinding

class PostDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_post_details)
    }
}