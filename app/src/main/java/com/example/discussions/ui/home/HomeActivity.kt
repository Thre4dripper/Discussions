package com.example.discussions.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private val TAG = "HomeActivity"

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        //temporarily disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding.bottomNavigationView.background = null
        binding.homeFab.setOnClickListener {
            val bottomSheet = CreatePostsBottomSheet()
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }

        initFragments()
    }

    private fun initFragments() {
        val discussFragment = DiscussFragment()
        val profileFragment = ProfileFragment()

        //initial fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, discussFragment, Constants.TAG_DISCUSS_FRAGMENT).commit()

        //bottom navigation listener
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, discussFragment, Constants.TAG_DISCUSS_FRAGMENT)
                        .commit()
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
                        .replace(R.id.fragment_container, profileFragment, Constants.TAG_PROFILE_FRAGMENT)
                        .commit()
                    true
                }
                else -> false
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentByTag(Constants.TAG_DISCUSS_FRAGMENT)

        //close app only when it is on discuss fragment
        if (fragment is DiscussFragment) {
            setResult(Constants.RESULT_CLOSE)
            finish()
        } else {
            //set discuss fragment, if not
            binding.bottomNavigationView.selectedItemId = R.id.navigation_home
        }
    }
}