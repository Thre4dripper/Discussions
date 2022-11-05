package com.example.discussions.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.discussions.databinding.FragmentProfileBinding
import com.example.discussions.ui.editProfile.EditProfileActivity

class ProfileFragment : Fragment() {
    lateinit var binding: FragmentProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        Glide.with(this)
            .load("https://media1.popsugar-assets.com/files/thumbor/8su-YMfzKmaONa2ODYaWx7dltHI/fit-in/500x500/filters:format_auto-!!-:strip_icc-!!-/2018/07/16/749/n/1922398/4c32bf875b4ccef29a4812.53698795_/i/Tom-Cruise.jpg")
            .into(binding.profileIv)

        binding.editProfileBtn.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }
}