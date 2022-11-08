package com.example.discussions.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.discussions.databinding.PostsBsLayoutBinding
import com.example.discussions.ui.createPost.CreatePostActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CreatePostsBottomSheet : BottomSheetDialogFragment() {
    lateinit var binding: PostsBsLayoutBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PostsBsLayoutBinding.inflate(inflater, container, false)

        binding.createPostBtn.setOnClickListener {
            startActivity(Intent(requireContext(), CreatePostActivity::class.java))
            dismiss()
        }
        return binding.root
    }
}