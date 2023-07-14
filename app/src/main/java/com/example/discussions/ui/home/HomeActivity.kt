package com.example.discussions.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.databinding.ActivityHomeBinding
import com.example.discussions.ui.bottomSheets.CreatePostsBS
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeActivity : AppCompatActivity() {
    private val TAG = "HomeActivity"

    private lateinit var binding: ActivityHomeBinding
    private lateinit var mode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        //temporarily disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding.bottomNavigationView.background = null
        binding.homeFab.setOnClickListener {
            val bottomSheet = CreatePostsBS()
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }

        //get username from intent
        val username = intent.getStringExtra(Constants.USERNAME)

        //when username is not null, it means that the activity is opened for profile
        mode = if (username != null) {
            hideBottomNavigation()
            Constants.HOME_ACTIVITY_PROFILE_MODE
        } else {
            showBottomNavigation()
            Constants.HOME_ACTIVITY_HOME_MODE
        }
        initPermissions()
        initFragments()
    }

    private fun hideBottomNavigation() {
        binding.bottomNavigationView.visibility = View.GONE
        binding.bottomAppBar.visibility = View.GONE
        binding.homeFab.visibility = View.GONE
    }

    private fun showBottomNavigation() {
        binding.bottomNavigationView.visibility = View.VISIBLE
        binding.bottomAppBar.visibility = View.VISIBLE
        binding.homeFab.visibility = View.VISIBLE
    }

    private fun initPermissions() {
        //request notification permission whether it is granted or not
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private var requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                when {
                    //this should handle the case when user has denied the permission but not permanently
                    shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) -> {
                        MaterialAlertDialogBuilder(this)
                            .setTitle("Notifications Permission")
                            .setMessage("Notifications permission is required to receive device notifications")
                            .setPositiveButton("Enable") { dialog, _ ->
                                dialog.dismiss()
                                initPermissions()
                            }
                            .setNegativeButton("Cancel") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }

                    //this should handle the case when user has permanently denied the permission
                    else -> {
                        MaterialAlertDialogBuilder(this)
                            .setTitle("Notifications Permission")
                            .setMessage("It seems like you have permanently denied notifications permission. You can enable it from settings")
                            .setPositiveButton("Settings") { dialog, _ ->
                                dialog.dismiss()
                                //open settings of the app
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts("package", packageName, null)
                                intent.data = uri
                                startActivity(intent)
                            }
                            .setNegativeButton("Cancel") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                }
            }
        }

    private fun initFragments() {
        val discussFragment = DiscussFragment()
        val pollsFragment = PollsFragment()
        val notificationFragment = NotificationFragment()
        val profileFragment = ProfileFragment()

        //initial fragment according to the mode
        if (mode == Constants.HOME_ACTIVITY_HOME_MODE) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, discussFragment, Constants.TAG_DISCUSS_FRAGMENT)
                .commit()
        } else if (mode == Constants.HOME_ACTIVITY_PROFILE_MODE) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, profileFragment, Constants.TAG_DISCUSS_FRAGMENT)
                .commit()
        }

        //bottom navigation listener
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            discussFragment,
                            Constants.TAG_DISCUSS_FRAGMENT
                        )
                        .commit()
                    true
                }

                R.id.navigation_poll -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            pollsFragment,
                            Constants.TAG_POLLS_FRAGMENT
                        )
                        .commit()
                    true
                }

                R.id.navigation_notification -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            notificationFragment,
                            Constants.TAG_NOTIFICATION_FRAGMENT
                        )
                        .commit()
                    true
                }

                R.id.navigation_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            profileFragment,
                            Constants.TAG_PROFILE_FRAGMENT
                        )
                        .commit()
                    true
                }

                else -> false
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        //back functionality is different for profile mode
        if (mode == Constants.HOME_ACTIVITY_PROFILE_MODE) {
            setResult(Constants.RESULT_CLOSE)
            finish()
            return
        }

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