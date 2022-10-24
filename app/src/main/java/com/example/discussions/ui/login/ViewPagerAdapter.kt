package com.example.discussions.ui.login

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout

class ViewPagerAdapter(
    fragmentManager: FragmentManager, lifecycle: Lifecycle, private var tabLayout: TabLayout
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return LoginFragment()
            1 -> return SignupFragment(tabLayout)
        }
        return LoginFragment()
    }
}