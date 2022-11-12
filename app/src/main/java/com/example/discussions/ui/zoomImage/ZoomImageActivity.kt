package com.example.discussions.ui.zoomImage

import android.os.Bundle
import android.util.Log
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

        val imageUrl = intent.getStringExtra(Constants.INTENT_IMAGE_URL)
        Log.d(TAG, "onCreate: $imageUrl")
        Glide.with(this).load(imageUrl).into(binding.zoomableIv)
    }
}