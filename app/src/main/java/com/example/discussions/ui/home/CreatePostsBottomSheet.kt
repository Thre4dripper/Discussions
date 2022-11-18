package com.example.discussions.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.discussions.Constants
import com.example.discussions.databinding.PostsBsLayoutBinding
import com.example.discussions.ui.CreateEditPostActivity
import com.example.discussions.viewModels.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CreatePostsBottomSheet : BottomSheetDialogFragment() {
    lateinit var binding: PostsBsLayoutBinding
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PostsBsLayoutBinding.inflate(inflater, container, false)
        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        binding.createPostBtn.setOnClickListener {
            val intent = Intent(context, CreateEditPostActivity::class.java)
            intent.putExtra(Constants.POST_MODE, Constants.MODE_CREATE_POST)
            startActivity(intent)
            dismiss()
        }
        return binding.root
    }
}