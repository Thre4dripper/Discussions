package com.example.discussions.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.databinding.ActivityZoomImageBinding

class ZoomImageActivity : AppCompatActivity() {
    private val TAG = "ZoomImageActivity"
    lateinit var binding: ActivityZoomImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_zoom_image)

        //TODO add title and content also here
        val imageUrl = intent.getStringExtra(Constants.ZOOM_IMAGE_URL)
        Glide.with(this).load(imageUrl).into(binding.zoomableIv)
    }
}