package com.example.discussions.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.example.discussions.R
import com.example.discussions.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        //temporarily disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding.bottomNavigationView.background = null

        initFragments()
    }

    private fun initFragments() {
        val discussFragment = DiscussFragment()
        val profileFragment = ProfileFragment()

        //initial fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, discussFragment).commit()

        //bottom navigation listener
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, discussFragment).commit()
                    true
                }
                R.id.navigation_poll -> {
                    //TODO: add poll fragment
                    true
                }
                R.id.navigation_notification -> {
                    //TODO: add notification fragment
                    true
                }
                R.id.navigation_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, profileFragment).commit()
                    true
                }
                else -> false
            }
        }
    }
}