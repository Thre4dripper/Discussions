package com.example.discussions.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.discussions.R
import com.example.discussions.databinding.ActivityCreateEditPostBinding

class EditPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateEditPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_edit_post)
    }
}