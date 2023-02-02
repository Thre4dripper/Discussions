package com.example.discussions.ui

import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.databinding.ActivityPostDetailsBinding
import com.example.discussions.viewModels.PostDetailsViewModel
import java.text.SimpleDateFormat
import java.util.*

class PostDetailsActivity : AppCompatActivity() {
    private val TAG = "PostDetailsActivity"

    private lateinit var binding: ActivityPostDetailsBinding
    private lateinit var viewModel: PostDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_post_details)

        viewModel = ViewModelProvider(this)[PostDetailsViewModel::class.java]

        //get post id from intent
        val postId = intent.getStringExtra(Constants.POST_ID)!!

        binding.postDetailsBackBtn.setOnClickListener {
            finish()
        }
        getPost(postId)
    }

    private fun getPost(postId: String) {
        //check if post is in post list
        if (viewModel.isPostInAlreadyFetched(postId)) {
            //if yes, get post from post repository
            viewModel.getPostFromPostRepository(postId)
            setPost()
        } else {
            //if not, get post from server
//            viewModel.getPostFromServer(postId)
        }
    }

    private fun setPost() {
        val post = viewModel.post
        //set user Image
        Glide.with(this)
            .load(post.username)
            .placeholder(R.drawable.ic_profile)
            .circleCrop()
            .into(binding.postDetailsUserImage)

        //set user name and time
        binding.postDetailsUsername.text = viewModel.post.username
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = dateFormat.parse(post.createdAt)

        binding.postDetailsTime.text = DateUtils.getRelativeTimeSpanString(
            date!!.time,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )

        //set post title and content
        binding.postDetailsTitle.apply {
            text = post.title
            visibility = if (post.title.isEmpty()) View.GONE else View.VISIBLE
        }
        binding.postDetailsContent.apply {
            text = post.content
            visibility = if (post.content.isEmpty()) View.GONE else View.VISIBLE
        }

        //set post image
        val image = post.postImage
        if (image != "") {
            binding.postDetailsImage.visibility = View.VISIBLE
            Glide.with(this)
                .load(image)
                .override(Target.SIZE_ORIGINAL)
                .into(binding.postDetailsImage)
            binding.postDetailsImage.setOnClickListener {
                val context = binding.postDetailsImage.context
                val intent = Intent(context, ZoomImageActivity::class.java)
                intent.putExtra(Constants.ZOOM_IMAGE_URL, image)
                context.startActivity(intent)
            }
        } else {
            binding.postDetailsImage.visibility = View.GONE
        }

        //set post like count
        binding.postDetailsLikesCount.text = post.likes.toString()

        //local variable for realtime like button change
        var postIsLiked = post.isLiked
        //setting like and comment button click listeners
        binding.postDetailsLikeBtn.apply {
            setOnClickListener {
                //like post logic

                //changing the like button icon every time it is clicked
                postIsLiked = !postIsLiked
                setCompoundDrawablesWithIntrinsicBounds(
                    if (postIsLiked) {
                        R.drawable.ic_like_filled
                    } else R.drawable.ic_like,
                    0,
                    0,
                    0
                )
                //changing the likes count every time the like button is clicked based on the current state of the post
                binding.postDetailsLikesCount.text =
                    if (postIsLiked) {
                        binding.postDetailsLikesCount.text.toString().toInt().plus(1).toString()
                    } else {
                        binding.postDetailsLikesCount.text.toString().toInt().minus(1).toString()
                    }
            }
            //checking if the current user has liked the post
            setCompoundDrawablesWithIntrinsicBounds(
                if (postIsLiked) {
                    R.drawable.ic_like_filled
                } else R.drawable.ic_like,
                0,
                0,
                0
            )
        }
    }
}